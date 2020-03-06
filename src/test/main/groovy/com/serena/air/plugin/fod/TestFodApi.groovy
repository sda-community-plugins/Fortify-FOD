package com.serena.air.plugin.fod

def fodServerUrl = "https://api.emea.fortify.com"
def fodPortalUrl = "https://emea.fortify.com"
def fodTenantCode = "emeademo"
def fodUser = System.getenv("FOD_CLIENT_ID")
def fodPassword = System.getenv("FOD_CLIENT_SECRET")
def fodProxyHost = "localhost"
def fodProxyPort = 1080
def fodProxyType = Proxy.Type.HTTP

//Proxy fodProxy = new Proxy(fodProxyType, new InetSocketAddress(fodProxyHost, fodProxyPort));
FodApi fodApi = new FodApi(fodServerUrl, null, fodPortalUrl)
fodApi.authenticate(fodTenantCode, fodUser, fodPassword, fodApi.GRANT_TYPE_CLIENT_CREDENTIALS)

def appId = 31122
def relId = 51667
def scanId = 221443
def entitlementId = 1459
def bsiToken = ""

//File dynamicScanFile = new File("C:\\Users\\klee\\Desktop\\http.simplesecureapp-production.eu-west-2.elasticbeanstalk.com 2020-02-19T10.25.34.fpr")
//File staticScanFile = new File()

// Start scan example(s)

/*
println "Starting dynamic scan for release: ${relId}"
if (fodApi.dynamicScanController.startDynamicScan(relId, false, FodEnums.DynamicAssessmentType.DynamicWebsiteAssessment,
        entitlementId, FodEnums.EntitlementFrequencyType.SingleScan, FodEnums.DynamicAssessmentType.DynamicWebsiteAssessment,
        false, true )) {
    scanId = fodApi.getDynamicScanController().getTriggeredScanId()
    println "Polling scan id ${scanId} for status"
    PollStatus listener = new PollStatus(fodApi, 1);
    boolean passFailPolicy = listener.releaseStatus(relId, scanId);
}

println "Starting static scan for release: ${relId}"
if (fodApi.staticScanController.startStaticScan(bsiToken, "C:\\Temp\\fodUpload", false,
        FodEnums.RemediationScanPreferenceType.NonRemediationScanOnly,
        FodEnums.EntitlementPreferenceType.SubscriptionOnly, false,
        FodEnums.InProgressScanActionType.DoNotStartScan, "")) {
    scanId = fodApi.getStaticScanController().getTriggeredScanId()
    println "Polling scan id ${scanId} for status"
    PollStatus listener = new PollStatus(fodApi, 1);
    boolean passFailPolicy = listener.releaseStatus(relId, scanId);
}
*/

// Import example(s)

/*
println "Importing dynamic scan for release: ${relId}"
if (fodApi.getDynamicScanController().importScanResults(FodEnums.ScanType.Dynamic, relId, scanFile)) {
    println "Reference Id: " + fodApi.getDynamicScanController().getImportReferenceId()
}

println "Importing static scan for release: ${relId}"
File scanFile = new File("C:\\Users\\klee\\Desktop\\http.simplesecureapp-production.eu-west-2.elasticbeanstalk.com 2020-02-19T10.25.34.fpr")
if (fodApi.getStaticScanController().importScanResults(FodEnums.ScanType.Static, relId, scanFile)) {
    println "Reference Id: " + fodApi.getStaticScanController().getImportReferenceId()
}

*/


// Export example(s)

/*def release = fodApi.getReleaseController().getReleaseById(relId)

String releaseUrl = "${fodPortalUrl}/Redirect/Releases/${release.getReleaseId()}"
String applicationUrl = "${fodPortalUrl}/Redirect/Applications/${release.getApplicationId()}"
def sb = new StringBuilder()
sb << """
<style type='text/css'>
    body { padding: 10px; 	background: #F5F5F5; font-family: Arial; }
    div { 
        background: #fff; box-shadow: 4px 4px 10px 0px rgba(119, 119, 119, 0.3);
        -moz-box-shadow: 4px 4px 10px 0px rgba(119, 119, 119, 0.3);
        -webkit-box-shadow: 4px 4px 10px 0px rgba(119, 119, 119, 0.3);
        margin-bottom: 20px; }
    a { color: #337ab7; text-decoration: underline; }
    a:hover { text-decoration: none; }
    span { font-weight: bold; }
    span.failed { color: #df1e1e; }
    span.success { color: #32ad12; }
    table { border-collapse: collapse; width: 100%; color: #333; }
    table td, table th { border: 1px solid #ddd; }
    table th { padding: 10px; text-align: center; background: #eee; font-size: 16px; }
    table td { padding: 7px; font-size: 12px; } " +
    table tr th:first-child { max-width: 200px; min-width: 200px; }
    table tr td:first-child { width: 200px; text-align: right; font-weight: bold;}
    table tr td:first-child:after {content: ':' }
</style>
"""
sb << "<div> <table> <tr> <th colspan='2'>Release: ${release.getReleaseName()} </th> </tr>"
sb << "<tr> <td> Application" + "</td> <td> <a href='${applicationUrl}'  target=_blank>" + release.getApplicationName() + "</a> </td> </tr>"
sb << "<tr> <td> Date Created </td> <td> ${release.getReleaseCreatedDate()} </td> </tr>"
sb << "<tr> <td> Description </td> <td> ${release.getReleaseDescription()} </td> </tr>"
sb << "<tr> <td> SDLC Status </td> <td> ${release.getSdlcStatusType()} </td> </tr>"
sb << "<tr> <td> Number of Criticals </td> <td> ${release.getCritical()} </td> </tr>"
sb << "<tr> <td> Number of Highs </td> <td> ${release.getHigh()} </td> </tr>"
sb << "<tr> <td> Number of Mediums </td> <td> ${release.getMedium()} </td> </tr>"
sb << "<tr> <td> Number of Lows </td> <td> ${release.getLow()} </td> </tr>"
sb << "<tr> <td> Star Rating </td> <td> ${release.getRating()} </td> </tr>"
sb << "<tr> <td> Pass/Fail status </td> <td> " +
        (release.isPassed() ? "<span class=\"success\">Passed</span>" : "<span class=\"failed\">Failed</span>") +
        " </td> </tr>"
if (!release.isPassed()) {
    String passFailReason = release.getPassFailReasonType() == null ?
            "Pass/Fail Policy requirements not met " :
            release.getPassFailReasonType()
    sb << "<tr> <td> Failure Reason: " + passFailReason + "</td> </tr>";
}
sb << "<tr> <td> URL" + "</td> <td> <a href='${releaseUrl}'  target=_blank>" + releaseUrl + "</a> </td> </tr>"
sb << "<br>"
sb << "</table> </div>"
sb << "</body>"
sb << "</html>"

String name = "release"
File f = new File(name + ".html")
BufferedWriter bw = new BufferedWriter(new FileWriter(f))
bw.write(sb.toString())
bw.close()
*/

/*
def scan = fodApi.getScanSummaryController().getScanSummary(relId, scanId)
String scanUrl = "${fodPortalUrl}/Redirect/Scans/${scan.getScanId()}"
String releaseUrl = "${fodPortalUrl}/Redirect/Releases/${scan.getReleaseId()}"
String applicationUrl = "${fodPortalUrl}/Redirect/Applications/${scan.getApplicationId()}"
def sb = new StringBuilder()
sb << """
<style type='text/css'>
    body { padding: 10px; 	background: #F5F5F5; font-family: Arial; }
    div { 
        background: #fff; box-shadow: 4px 4px 10px 0px rgba(119, 119, 119, 0.3);
        -moz-box-shadow: 4px 4px 10px 0px rgba(119, 119, 119, 0.3);
        -webkit-box-shadow: 4px 4px 10px 0px rgba(119, 119, 119, 0.3);
        margin-bottom: 20px; }
    a { color: #337ab7; text-decoration: underline; }
    a:hover { text-decoration: none; }
    span { font-weight: bold; }
    span.failed { color: #df1e1e; }
    span.success { color: #32ad12; }
    table { border-collapse: collapse; width: 100%; color: #333; }
    table td, table th { border: 1px solid #ddd; }
    table th { padding: 10px; text-align: center; background: #eee; font-size: 16px; }
    table td { padding: 7px; font-size: 12px; } " +
    table tr th:first-child { max-width: 200px; min-width: 200px; }
    table tr td:first-child { width: 200px; text-align: right; font-weight: bold;}
    table tr td:first-child:after {content: ':' }
</style>
"""
sb << "<div> <table> <tr> <th colspan='2'>${scan.getScanType()} Scan: ${scan.getScanId()} </th> </tr>"
sb << "<tr> <td> Application" + "</td> <td> <a href='${applicationUrl}'  target=_blank>" + scan.getApplicationName() + "</a> </td> </tr>"
sb << "<tr> <td> Release" + "</td> <td> <a href='${releaseUrl}'  target=_blank>" + scan.getReleaseName() + "</a> </td> </tr>"
sb << "<tr> <td> Assessment Type </td> <td> ${scan.getAssessmentTypeName()} </td> </tr>"
sb << "<tr> <td> Date Started </td> <td> ${scan.getStartedDatetime()} </td> </tr>"
sb << "<tr> <td> Date Completed </td> <td> ${scan.getCompletedDateTime()} </td> </tr>"
sb << "<tr> <td> Notes </td> <td> ${scan.getNotes()} </td> </tr>"
sb << "<tr> <td> Scan Tool </td> <td> ${scan.getScanTool()} - ${scan.getScanToolVersion()} </td> </tr>"
sb << "<tr> <td> Number of Criticals </td> <td> ${scan.getIssueCountCritical()} </td> </tr>"
sb << "<tr> <td> Number of Highs </td> <td> ${scan.getIssueCountHigh()} </td> </tr>"
sb << "<tr> <td> Number of Mediums </td> <td> ${scan.getIssueCountMedium()} </td> </tr>"
sb << "<tr> <td> Number of Lows </td> <td> ${scan.getIssueCountLow()} </td> </tr>"
sb << "<tr> <td> Star Rating </td> <td> ${scan.getStarRating()} </td> </tr>"
sb << "<tr> <td> Analysis status </td> <td> " +
        (scan.getAnalysisStatusType().equals("Completed") ? "<span class=\"success\">Completed</span>" : "<span class=\"failed\">${scan.getAnalysisStatusType()}</span>") +
        " </td> </tr>"
sb << "<tr> <td> URL" + "</td> <td> <a href='${scanUrl}'  target=_blank>" + scanUrl + "</a> </td> </tr>"
sb << "<br>"
sb << "</table> </div>"
sb << "</body>"
sb << "</html>"

String name = "scan"
File f = new File(name + ".html")
BufferedWriter bw = new BufferedWriter(new FileWriter(f))
bw.write(sb.toString())
bw.close()
*/

fodApi.retireToken()

