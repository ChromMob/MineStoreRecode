package me.chrommob.minestore.common.dumper;

import com.google.gson.Gson;

import javax.net.ssl.HttpsURLConnection;

import java.io.IOException;
import java.net.URL;

public class Dumper {
    private final Gson gson = new Gson();

    public String dump(boolean includeLog) {
        // Do post request with "randomData" as the body to dumpLink
        try {
            String dumpLink = "https://paste.chrommob.fun/";
            String postLink = dumpLink + "post";
            URL url = new URL(postLink);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            DumpData dumpData = new DumpData(includeLog);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.getOutputStream().write(gson.toJson(dumpData).getBytes());
            connection.getOutputStream().flush();
            connection.getOutputStream().close();
            connection.connect();
            if (connection.getResponseCode() == 201) {
                return dumpLink + connection.getHeaderField("Location");
            } else {
                if (connection.getResponseCode() == 413) {
                    return dump(false);
                }
                return connection.getResponseMessage() + " (" + connection.getResponseCode() + ")";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
