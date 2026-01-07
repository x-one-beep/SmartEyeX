package com.example.smarteyex;

import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;

public class AICore {

    public String analyzeNotification(String text) {
        return callGroq("Ringkas notifikasi ini: " + text);
    }

    public String getWeatherInsight(String location) {
        String weatherToken = "e3595150-e17f-11f0-a8f4-0242ac130003-e35951b4-e17f-11f0-a8f4-0242ac130003";
        return "Cuaca di " + location + ": Cerah, 25°C";
    }

    private String callGroq(String prompt) {
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        json.put("model", "llama3-8b-8192");
        json.put("messages", new JSONArray().put(new JSONObject().put("role", "user").put("content", prompt)));

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(GROQ_URL)
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                JSONObject resJson = new JSONObject(response.body().string());
                return resJson.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
            }
        } catch (IOException | org.json.JSONException e) {
            e.printStackTrace();
        }
        return "Error in AI response";
    }
}
