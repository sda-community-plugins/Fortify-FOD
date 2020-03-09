package com.serena.air.plugin.fod.controllers;

import com.google.gson.Gson;
import com.serena.air.plugin.fod.FodApi;
import com.serena.air.plugin.fod.FodEnums;
import com.serena.air.plugin.fod.models.GenericErrorResponse;
import com.serena.air.plugin.fod.models.PostStartScanResponse;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;

import java.io.FileInputStream;
import java.util.Arrays;

public class StaticScanController extends ScanControllerBase {

    /**
     * Constructor
     *
     * @param api api object with client info
     */
    public StaticScanController(final FodApi api) {
        super(api);
    }

    /**
     * Starts a scan based on the V3 API
     *
     * @param
     * @return true if scan successfully started
     */
    public boolean startStaticScan(String bsiToken, String payload, boolean isRemediationScan,
                                   FodEnums.RemediationScanPreferenceType remediationScanPreference,
                                   FodEnums.EntitlementPreferenceType entitlementPreference, boolean purchaseEntitlement,
                                   FodEnums.InProgressScanActionType inProgressScanPreferenceType, String notes) {
        PostStartScanResponse scanStartedResponse;

        try {
            FileInputStream fs = new FileInputStream(payload);
            byte[] readByteArray = new byte[CHUNK_SIZE];
            byte[] sendByteArray;
            int fragmentNumber = 0;
            int byteCount;
            long offset = 0;

            parsedbsiToken = parser.convert(bsiToken);
            if (parsedbsiToken == null) {
                throw new Exception("Bsi Token given is invalid and cannot be parsed");
            }
            remediationScanPreference = (isRemediationScan) ? FodEnums.RemediationScanPreferenceType.RemediationScanOnly
                    : remediationScanPreference != null ? remediationScanPreference : FodEnums.RemediationScanPreferenceType.NonRemediationScanOnly ;
            HttpUrl.Builder builder = HttpUrl.parse(api.getBaseUrl()).newBuilder()
                    .addPathSegments(String.format("/api/v3/releases/%d/static-scans/start-scan-advanced", parsedbsiToken.getProjectVersionId()))
                    .addQueryParameter("releaseId", Integer.toString(parsedbsiToken.getProjectVersionId()))
                    .addQueryParameter("bsiToken", bsiToken.toString())
                    .addQueryParameter("entitlementPreferenceType", (entitlementPreference != null) ? entitlementPreference.toString() : "3")
                    .addQueryParameter("purchaseEntitlement", Boolean.toString(purchaseEntitlement))
                    .addQueryParameter("remdiationScanPreferenceType", (remediationScanPreference != null) ? remediationScanPreference.toString() : "2")
                    .addQueryParameter("inProgressScanActionType", (inProgressScanPreferenceType != null) ? inProgressScanPreferenceType.toString() : "0")
                    .addQueryParameter("scanTool", FodEnums.fodScanTool)
                    .addQueryParameter("scanToolVersion", FodEnums.fodScanToolVersion)
                    .addQueryParameter("scanMethodType", FodEnums.fodScanMethodType);
            if (notes != null && !notes.isEmpty()) {
                String truncatedNotes = abbreviateString(notes.trim(), MAX_NOTES_LENGTH);
                builder = builder.addQueryParameter("notes", truncatedNotes);
            }
            // TODO: Come back and fix the request to set fragNo and offset query parameters
            String fragUrl = builder.build().toString();

            // Loop through chunk
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
                        .url(fragUrl + "&fragNo=" + fragmentNumber++ + "&offset=" + offset)
                        .post(RequestBody.create(byteArray, sendByteArray))
                        .build();
                // Get the response
                Response response = api.getClient().newCall(request).execute();
                if (response.code() == HttpStatus.SC_FORBIDDEN) {  // got logged out during polling so log back in
                    // Re-authenticate
                    api.authenticate();

                    // if you had to re-authenticate here, would the loop and request not need to be resubmitted?
                    // possible continue?
                }

                offset += byteCount;

                if (fragmentNumber % 5 == 0) {
                    System.out.println("Upload Status - Bytes sent:" + offset);
                }

                if (response.code() != 202) {
                    String responseJsonStr = IOUtils.toString(response.body().byteStream(), "utf-8");

                    Gson gson = new Gson();
                    if (response.code() == 200) {
                        scanStartedResponse = gson.fromJson(responseJsonStr, PostStartScanResponse.class);
                        System.out.println("Scan " + scanStartedResponse.getScanId() +
                                " uploaded successfully. Total bytes sent: " + offset);
                        triggeredScanId = scanStartedResponse.getScanId();
                        return true;
                    } else if (!response.isSuccessful()) {
                        GenericErrorResponse errors = gson.fromJson(responseJsonStr, GenericErrorResponse.class);
                        System.out.println("Package upload failed for the following reasons: " +
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

}
