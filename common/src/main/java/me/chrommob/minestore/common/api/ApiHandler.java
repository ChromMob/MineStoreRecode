package me.chrommob.minestore.common.api;

import com.google.gson.Gson;
import me.chrommob.minestore.api.generic.AuthData;
import me.chrommob.minestore.api.web.Result;
import me.chrommob.minestore.api.web.WebApiAccessor;
import me.chrommob.minestore.api.web.WebApiRequest;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class ApiHandler {
    private final Gson gson = new Gson();
    private final AuthData authData;
    public ApiHandler(AuthData authData) {
        this.authData = authData;
        WebApiAccessor.registerRequestHandler(this::request);
    }

    private <V> Result<V, Exception> request(WebApiRequest<V> request) {
        URL url = authData.createUrl(request.getPath(), request.getParams());
        try {
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod(request.getType().name().toUpperCase());
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            if (urlConnection.getResponseCode() != 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()))) {
                    StringBuilder responseString = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseString.append(line);
                    }
                    return new Result<>(gson.fromJson(responseString.toString(), request.getClazz()), null);
                }
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                StringBuilder responseString = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseString.append(line);
                }
                return new Result<>(gson.fromJson(responseString.toString(), request.getClazz()), null);
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return new Result<>(null, e);
        }
    }
}
