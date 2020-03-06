package com.serena.air.plugin.fod.controllers

import com.google.gson.*
import com.serena.air.plugin.fod.FodApi
import com.serena.air.plugin.fod.FodEnums
import com.serena.air.plugin.fod.models.LookupItemsModel
import com.serena.air.plugin.fod.utils.DateDeserializer
import okhttp3.Request
import okhttp3.Response
import org.apache.commons.io.IOUtils

public class LookupItemsController extends ControllerBase {
    /**
     * Constructor
     * @param api api object with client info
     */
    public LookupItemsController(FodApi api) {
        super(api);
    }

    /**
     * GET given enum
     * @param type enum to look up
     * @return array of enum values and text or null
     */
    public LookupItemsModel[] getLookupItems(FodEnums.APILookupItemTypes type) {
        try {
            Request request = new Request.Builder()
                    .url(api.getBaseUrl() + "/api/v3/lookup-items?type=" + type.toString())
                    .addHeader("Authorization", "Bearer " + api.getToken())
                    .get()
                    .build();
            Response response = api.getClient().newCall(request).execute();

            // Read the results and close the response
            String content = IOUtils.toString(response.body().byteStream(), "utf-8");
            response.body().close();

            // Parse the response to get a single Release object
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Date.class, new DateDeserializer());
            Gson gson = gsonBuilder.create();
            JsonElement jsonElement = new JsonParser().parse(content);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            LookupItemsModel[] results = gson.fromJson(jsonObject.getAsJsonArray("items"), LookupItemsModel[].class);
            return results;
            //Gson gson = new Gson();
            //Type t = new TypeToken<GenericListResponse<LookupItemsModel>>(){}.getType();
            //GenericListResponse<LookupItemsModel> results =  gson.fromJson(content, t);
            //return results.getItems();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
