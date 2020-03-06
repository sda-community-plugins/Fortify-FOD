package com.serena.air.plugin.fod.controllers

import com.fortify.fod.parser.BsiToken
import com.google.gson.Gson
import com.serena.air.plugin.fod.FodApi
import com.serena.air.plugin.fod.FodEnums
import com.serena.air.plugin.fod.models.GenericErrorResponse
import com.serena.air.plugin.fod.models.PostImportScanResponse
import com.serena.air.plugin.fod.models.PostImportScanSessionResponse
import com.serena.air.plugin.fod.parser.converters.BsiTokenConverter
import okhttp3.*
import org.apache.commons.io.IOUtils
import org.apache.http.HttpStatus

class ScanControllerBase extends ControllerBase {
    protected final int CHUNK_SIZE = 1024 * 1024;
    protected final int MAX_NOTES_LENGTH = 250;
    protected int triggeredScanId = -1;
    protected String importReferenceId;
    protected BsiToken parsedbsiToken = null;
    protected static BsiTokenConverter parser = new BsiTokenConverter();

    /**
     * Constructor
     *
     * @param api api object with client info
     */
    public ScanControllerBase(final FodApi api) {
        super(api);
    }

    public boolean importScanResults(FodEnums.ScanType scanType, int releaseId, File scanFile) {
        PostImportScanResponse importScanResponse;

        if (!scanFile.exists()) {
            throw new Exception("Scan File \"${scanFile}\" does not exist!");
        }

        String scanText = "static scan"
        String scanUrl = "/api/v3/releases/${releaseId}/static-scans/import-scan"
        if (this.getClass().getSimpleName() == "DynamicScanController") {
            scanText = "dynamic scan"
            scanUrl = "/api/v3/releases/${releaseId}/dynamic-scans/import-scan"

        }

        // start the import session
        String sessionId = startImportScanSession(releaseId);

        try {
            // upload data
            byte[] readByteArray = new byte[CHUNK_SIZE]
            byte[] sendByteArray
            int fragmentNumber = 0
            int byteCount
            long offset = 0
            long fileLength = scanFile.length()

            HttpUrl.Builder builder = HttpUrl.parse("https://api.emea.fortify.com").newBuilder()
                    .addPathSegments(String.format("/api/v3/releases/%s/dynamic-scans/import-scan", releaseId))
                    .addQueryParameter("releaseId", (releaseId as String))
                    .addQueryParameter("fileLength", (fileLength as String))
                    .addQueryParameter("importScanSessionId", sessionId)
            String fragUrl = builder.build().toString();

            FileInputStream fs = new FileInputStream(scanFile)

            while ((byteCount = fs.read(readByteArray)) != -1) {
                if (byteCount < CHUNK_SIZE) {
                    sendByteArray = Arrays.copyOf(readByteArray, byteCount);
                    fragmentNumber = -1;
                } else {
                    sendByteArray = readByteArray;
                }

                MediaType byteArray = MediaType.parse("application/octet-stream");
                Request request = new Request.Builder()
                        .addHeader("Authorization", "Bearer " + api.getToken())
                        .addHeader("Content-Type", "application/octet-stream")
                // Add offsets
                        .url(fragUrl + "&fragNo=" + fragmentNumber + "&offset=" + offset)
                        .put(RequestBody.create(byteArray, sendByteArray))
                        .build();

                // Get the response
                Response response = api.getClient().newCall(request).execute();
                if (response.code() == HttpStatus.SC_FORBIDDEN) {  // got logged out during polling so log back in
                    // Re-authenticate
                    api.authenticate();

                    // if you had to re-authenticate here, would the loop and request not need to be resubmitted?
                    // possible continue?
                }

                fragmentNumber++
                offset += byteCount;

                if (fragmentNumber % 5 == 0 && fragmentNumber != 0) {
                    System.out.println("Import Status - Bytes sent: " + offset);
                }

                if (response.code() != 202) {
                    String responseJsonStr = IOUtils.toString(response.body().byteStream(), "utf-8");

                    Gson gson = new Gson();
                    if (response.code() == HttpStatus.SC_OK) {
                        importScanResponse = gson.fromJson(responseJsonStr, PostImportScanResponse.class);
                        System.out.println("Started ${scanText} import with reference id: " + importScanResponse.getReferenceId() +
                                ". Total bytes sent: " + offset);
                        importReferenceId = importScanResponse.getReferenceId();
                        return true;
                    } else if (!response.isSuccessful()) {
                        GenericErrorResponse errors = gson.fromJson(responseJsonStr, GenericErrorResponse.class);
                        System.out.println("Import scan failed for the following reasons: " +
                                errors.toString());
                        return false;
                    }
                }
                response.body().close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private String startImportScanSession(int releaseId) {
        PostImportScanSessionResponse importScanSessionResponse;
        String sessionId = null;

        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + api.getToken())
                .url(api.getBaseUrl() + "/api/v3/releases/${releaseId}/import-scan-session-id")
                .get()
                .build();
        Response response = api.getClient().newCall(request).execute();
        String responseJsonStr = IOUtils.toString(response.body().byteStream(), "utf-8");

        Gson gson = new Gson();
        if (response.code() == HttpStatus.SC_OK) {
            importScanSessionResponse = gson.fromJson(responseJsonStr, PostImportScanSessionResponse.class);
            System.out.println("Initiating import scan session: " + importScanSessionResponse.getImportScanSessionId());
            sessionId = importScanSessionResponse.getImportScanSessionId();
        } else if (!response.isSuccessful()) {
            GenericErrorResponse errors = gson.fromJson(responseJsonStr, GenericErrorResponse.class);
            System.out.println("Initiating import scan session failed for the following reasons: " +
                    errors.toString());
        }
        response.body().close();
        return sessionId;
    }

    protected static String abbreviateString(String input, int maxLength) {
        if (input.length() <= maxLength)
            return input;
        else
            return input.substring(0, maxLength);
    }

    public int getTriggeredScanId() {
        return triggeredScanId;
    }

    public String getImportReferenceId() {
        return importReferenceId;
    }
}
