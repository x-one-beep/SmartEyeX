package com.smarteyex.ai;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import java.util.Locale;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AudioEngine {
    private TextToSpeech tts;
    private OkHttpClient client = new OkHttpClient();
    public AudioEngine(Context context) {
        try {
            tts = new TextToSpeech(context, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.getDefault());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void speak(String text) {
        try {
            String aiResponse = processWithAI(text);  // Integrasi AI
            tts.speak(aiResponse != null ? aiResponse : text, TextToSpeech.QUEUE_FLUSH, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String processWithAI(String input) {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("model", "llama3-8b-8192");
            json.addProperty("messages", "[{\"role\": \"user\", \"content\": \"" + input + "\"}]");
            Request request = new Request.Builder()
                .url("https://api.groq.com/openai/v1/chat/completions")
                .addHeader("Content-Type", "application/json")
                .post(okhttp3.RequestBody.create(json.toString(), okhttp3.MediaType.parse("application/json")))
                .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                JsonObject responseJson = JsonParser.parseString(response.body().string()).getAsJsonObject();
                return responseJson.getAsJsonArray("choices").get(0).getAsJsonObject().get("message").getAsJsonObject().get("content").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;  // Fallback
    }
}
