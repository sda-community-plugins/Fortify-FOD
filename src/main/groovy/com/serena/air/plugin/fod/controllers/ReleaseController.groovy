package com.serena.air.plugin.fod.controllers

import com.google.gson.*
import com.serena.air.plugin.fod.FodApi
import com.serena.air.plugin.fod.models.ReleaseDTO
import com.serena.air.plugin.fod.utils.DateDeserializer
import okhttp3.Request
import okhttp3.Response
import org.apache.commons.io.IOUtils
import org.apache.http.HttpStatus

class ReleaseController extends ControllerBase {
    /**
     * Constructor
     *
     * @param api api object with client info
     */
    public ReleaseController(FodApi api) {
        super(api);
    }

    /**
     * GET specific release by external Id
     *
     * @param releaseId release to get
     * @return returns ReleaseDTO object containing specified fields or null
     */
    public ReleaseDTO getReleaseById(int releaseId) {
        try {
            String url = api.getBaseUrl() + "/api/v3/releases/" + releaseId;
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + api.getToken())
                    .get()
                    .build();
            Response response = api.getClient().newCall(request).execute();
            // The endpoint call was unsuccessful. Maybe unauthorized who knows.
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code: " + response);
            }
            if (response.code() == HttpStatus.SC_UNAUTHORIZED) {  // got logged out during polling so log back in
                System.out.println("Token expired re-authorizing");
                // Re-authenticate
                api.authenticate();
            }

            // Read the results and close the response
            String content = IOUtils.toString(response.body().byteStream(), "utf-8");
            response.body().close();

            // Parse the response to get a single Release object
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Date.class, new DateDeserializer());
            Gson gson = gsonBuilder.create();
            ReleaseDTO release = gson.fromJson(content, ReleaseDTO.class);
            return release;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * GET specific release by internal Id with given fields if applicable
     *
     * @param releaseId release to get
     * @param fields    specific fields to return
     * @return returns ReleaseDTO object containing specified fields or null
     */
    public ReleaseDTO getReleaseFields(int releaseId, String fields) {
        try {
            String url = api.getBaseUrl() + "/api/v3/releases?filters=releaseId:" + releaseId;
            if (fields.length() > 0) {
                url += "&fields=" + fields + "&limit=1";
            }
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + api.getToken())
                    .get()
                    .build();
            Response response = api.getClient().newCall(request).execute();

            // The endpoint call was unsuccessful. Maybe unauthorized who knows.
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code: " + response);
            }
            if (response.code() == HttpStatus.SC_UNAUTHORIZED) {  // got logged out during polling so log back in
                System.out.println("Token expired re-authorizing");
                // Re-authenticate
                api.authenticate();
            }

            // Read the results and close the response
            String content = IOUtils.toString(response.body().byteStream(), "utf-8");
            response.body().close();

            // Parse the response to get a single Release object
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Date.class, new DateDeserializer());
            Gson gson = gsonBuilder.create();
            JsonElement jsonElement = new JsonParser().parse(content);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            ReleaseDTO release = gson.fromJson(jsonObject.getAsJsonArray("items").get(0), ReleaseDTO.class);
            return release;
            // Create a type of GenericList<ReleaseDTO> to play nice with gson.
            //Type t = new TypeToken<GenericListResponse<ReleaseDTO>>() {
            //}.getType();
            //GenericListResponse<ReleaseDTO> results = gson.fromJson(content, t);
            //System.out.println(results.getItems().length);
            //return results.getItems()[0];
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
