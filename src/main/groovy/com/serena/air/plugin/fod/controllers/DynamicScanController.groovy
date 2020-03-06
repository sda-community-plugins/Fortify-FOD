package com.serena.air.plugin.fod.controllers

import com.google.gson.Gson
import com.serena.air.plugin.fod.FodApi
import com.serena.air.plugin.fod.FodEnums
import com.serena.air.plugin.fod.models.GenericErrorResponse
import com.serena.air.plugin.fod.models.PostStartScanResponse
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.apache.commons.io.IOUtils
import org.apache.http.HttpStatus
import org.json.JSONException
import org.json.JSONObject

import java.text.SimpleDateFormat

class DynamicScanController extends ScanControllerBase {

    /**
     * Constructor
     *
     * @param api api object with client info
     */
    public DynamicScanController(final FodApi api) {
        super(api);
    }

    /**
     * Starts a dynamic based on the V3 API
     *
     * @param
     * @return true if scan successfully started
     */
    public boolean startDynamicScan(int releaseId, boolean isRemediationScan, FodEnums.DynamicAssessmentType assessmentType,
                                    int entitlementId, FodEnums.EntitlementFrequencyType entitlementFrequencyType,
                                    FodEnums.DynamicAssessmentType parentAssessmentTypeId, boolean isBundledAssessment,
                                    boolean applyPreviousScanSettings) {
        PostStartScanResponse scanStartedResponse;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            String curDateTime = sdf.format(new Date())
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("startDate", curDateTime);
                jsonObject.put("assessmentTypeId", assessmentType.getValue());
                jsonObject.put("entitlementId", entitlementId);
                jsonObject.put("entitlementFrequencyType", entitlementFrequencyType.toString());
                jsonObject.put("isRemediationScan", isRemediationScan);
                jsonObject.put("isBundledAssessment", isBundledAssessment);
                jsonObject.put("parentAssessmentTypeId", parentAssessmentTypeId.getValue());
                jsonObject.put("applyPreviousScanSettings", applyPreviousScanSettings);
                jsonObject.put("scanMethodType", FodEnums.fodScanMethodType);
                jsonObject.put("scanTool", FodEnums.fodScanTool);
                jsonObject.put("scanToolVersion", FodEnums.fodScanToolVersion);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON, jsonObject.toString())
            Request request = new Request.Builder()
                    .addHeader("Authorization", "Bearer " + api.getToken())
                    .url(api.getBaseUrl() + "/api/v3/releases/${releaseId}/dynamic-scans/start-scan")
                    .post(requestBody)
                    .build();
            Response response = api.getClient().newCall(request).execute();
            String responseJsonStr = IOUtils.toString(response.body().byteStream(), "utf-8");

            Gson gson = new Gson();
            if (response.code() == HttpStatus.SC_OK) {
                scanStartedResponse = gson.fromJson(responseJsonStr, PostStartScanResponse.class);
                System.out.println("Scan " + scanStartedResponse.getScanId() +
                        " started successfully.");
                triggeredScanId = scanStartedResponse.getScanId();
                return true;
            } else if (!response.isSuccessful()) {
                GenericErrorResponse errors = gson.fromJson(responseJsonStr, GenericErrorResponse.class);
                System.out.println("Scan failed for the following reasons: " +
                        errors.toString());
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
}
