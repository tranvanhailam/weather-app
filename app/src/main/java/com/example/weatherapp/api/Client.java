package com.example.weatherapp.api;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Client {

    public static JSONObject request(String url) throws IOException {

         OkHttpClient http = new OkHttpClient();

        Request req = new Request.Builder().url(url).get().build();
        try (Response res = http.newCall(req).execute()) {
            if (!res.isSuccessful()) throw new IOException("HTTP " + res.code());
            String body = new String(res.body().bytes(), StandardCharsets.UTF_8);
            return new JSONObject(body);
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception ex) {
            throw new IOException("Parse error: " + ex.getMessage(), ex);
        }
    }
}
