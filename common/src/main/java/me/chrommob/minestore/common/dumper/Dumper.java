package me.chrommob.minestore.common.dumper;

import com.google.gson.Gson;
import me.chrommob.minestore.common.MineStoreCommon;

import javax.net.ssl.HttpsURLConnection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Dumper {
    private final Gson gson = new Gson();
    private final String LOG = "Log file not found or too large to dump.";

    public String dump(String log, MineStoreCommon plugin) {
        try {
            String dumpLink = "https://paste.chrommob.fun/";
            String postLink = dumpLink + "post";
            URL url = new URL(postLink);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            DumpData dumpData = new DumpData(log, plugin);
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
                    return dump(false, plugin);
                }
                return connection.getResponseMessage() + " (" + connection.getResponseCode() + ")";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String dump(boolean includeLog, MineStoreCommon plugin) {
        // Do post request with "randomData" as the body to dumpLink
        Path logPath = Paths.get(
                plugin.jarFile().getParentFile().getParentFile().getAbsolutePath(), "logs",
                "latest.log");
        File logFile = logPath.toFile();
        if (logFile.exists()) {
            try {
                StringBuilder fileData = new StringBuilder();
                BufferedReader reader = new BufferedReader(
                        new FileReader(logFile));
                char[] buf = new char[1024];
                int numRead;
                while ((numRead = reader.read(buf)) != -1) {
                    String readData = String.valueOf(buf, 0, numRead);
                    fileData.append(readData);
                }
                reader.close();
                String log = fileData.toString();
                return dump(log, plugin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dump(LOG, plugin);
    }
}
