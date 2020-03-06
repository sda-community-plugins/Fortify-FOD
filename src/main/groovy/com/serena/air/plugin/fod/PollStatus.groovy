package com.serena.air.plugin.fod

import com.serena.air.plugin.fod.models.LookupItemsModel
import com.serena.air.plugin.fod.models.ReleaseDTO
import com.serena.air.plugin.fod.models.ScanSummaryDTO

class PollStatus {
    private FodApi fodApi;
    private int pollingInterval;
    private int failCount = 0;
    private final int MAX_FAILS = 3;

    private List<LookupItemsModel> analysisStatusTypes = null;

    /**
     * Constructor
     * @param api api connection to use
     * @param pollingInterval interval to poll
     */
    PollStatus(final FodApi api, final int pollingInterval) {
        fodApi = api;
        this.pollingInterval = pollingInterval;
    }

    /**
     * Polls the release status
     * @param releaseId release to poll
     * @return true if status is completed | cancelled.
     */
    boolean releaseStatus(final int releaseId, final int triggeredScanId) {
        boolean finished = false;  // default is failure
        boolean policyPass = true;
        try
        {
            while(!finished)
            {
                // Get the status of the release
                ReleaseDTO release = fodApi.getReleaseController().getReleaseFields(releaseId,
                        "currentAnalysisStatusTypeId,isPassed,passFailReasonId,critical,high,medium,low,rating");
                if (release == null) {
                    failCount++;
                    continue;
                }

                int status = release.getCurrentAnalysisStatusTypeId();

                // Get the possible statuses only once
                if(analysisStatusTypes == null)
                    analysisStatusTypes = Arrays.asList(fodApi.getLookupController().getLookupItems(FodEnums.APILookupItemTypes.AnalysisStatusTypes));

                if(failCount < MAX_FAILS)
                {
                    String statusString = "";

                    // Create a list of values that will be used to break the loop if found
                    // This way if any of this changes we don't need to redo the keys or something
                    List<String> complete = new ArrayList<>();
                    for (LookupItemsModel statusType : analysisStatusTypes) {
                        if (statusType.getText() == "Completed")    complete.add(statusType.getValue())
                        if (statusType.getText() == "Canceled")     complete.add(statusType.getValue())
                        if (statusType.getText() == "Waiting")      complete.add(statusType.getValue())
                    }

                    // Look for and print the status OR break the loop.
                    for(LookupItemsModel o: analysisStatusTypes) {
                        if(o != null) {
                            int analysisStatus = Integer.parseInt(o.getValue());
                            if (analysisStatus == status) {
                                statusString = o.getText().replace("_", " ");
                            }
                            if (complete.contains(Integer.toString(status))) {
                                finished = true;
                            }
                        }
                    }
                    System.out.println("Poll Status: " + statusString);
                    if (statusString.equals("Canceled") || statusString.equals("Waiting") ) {
                        ScanSummaryDTO scanSummary = fodApi.getScanSummaryController().getScanSummary(releaseId,triggeredScanId);
                        String message = statusString.equals("Canceled") ? "-------Scan Canceled-------" : "-------Scan Paused-------";
                        String reason = statusString.equals("Canceled") ? "Cancel reason:        %s" : "Pause reason:        %s";
                        String reasonNotes = statusString.equals("Canceled") ? "Cancel reason notes:  %s" : "Pause reason notes:  %s";
                        if (scanSummary == null) {
                            System.out.println("Unable to retrieve scan summary data");
                        } else {
                            System.out.println(message);
                            int pauseDetailsLength = scanSummary.getPauseDetails().length > 0 ? scanSummary.getPauseDetails().length : 0;
                            System.out.println(String.format(reason, statusString.equals("Canceled") ? scanSummary.getCancelReason()
                                    :  ((pauseDetailsLength > 0 ) ? (scanSummary.getPauseDetails()[pauseDetailsLength-1].getReason() == null) ?"" : scanSummary.getPauseDetails()[pauseDetailsLength-1].getReason(): "")));
                            System.out.println(String.format(reasonNotes, statusString.equals("Canceled") ? scanSummary.getAnalysisStatusReasonNotes()
                                    :  ((pauseDetailsLength > 0 ) ? (scanSummary.getPauseDetails()[pauseDetailsLength-1].getNotes() == null) ? "" : scanSummary.getPauseDetails()[pauseDetailsLength-1].getNotes()  : "")));
                            System.out.println();
                        }

                    }
                    if(statusString.equals("Completed"))
                    {
                        policyPass = printPassFail(release, releaseId);
                    }
                }
                else
                {
                    System.out.println("getStatus failed 3 consecutive times terminating polling");
                    finished = true;
                }
                if (!finished) Thread.sleep(pollingInterval*60*1000);

            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        return policyPass;
    }

    /**
     * Prints some info about the release including a vuln breakdown and pass/fail reason
     * @param release release to print info on
     */
    private boolean printPassFail(ReleaseDTO release, int releaseId) {
        try
        {
            // Break if release is null
            if (release == null) {
                this.failCount++;
                return false;
            }
            System.out.println("Star Rating: " + release.getRating())
            System.out.println("Number of Criticals: " +  release.getCritical());
            System.out.println("Number of Highs: " +  release.getHigh());
            System.out.println("Number of Mediums: " +  release.getMedium());
            System.out.println("Number of Lows: " +  release.getLow());
            System.out.println("For application status details see the customer portal: ");
            System.out.println(String.format("%s/Redirect/Releases/%d", fodApi.getPortalUri(), releaseId));
            boolean isPassed = release.isPassed();
            System.out.println("Pass/Fail Status: " + (isPassed ? "Passed" : "Failed") );
            if (!isPassed)
            {
                String passFailReason = release.getPassFailReasonType() == null ?
                        "Pass/Fail Policy requirements not met " :
                        release.getPassFailReasonType();

                System.out.println("Failure Reason: " + passFailReason);
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
