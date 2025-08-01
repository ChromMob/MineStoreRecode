package me.chrommob.minestore.common.dumper;

import com.google.gson.Gson;
import me.chrommob.minestore.common.MineStoreCommon;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

public class Dumper {
    private final Gson gson = new Gson();

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
            return null;
        }
    }

    public String dump(boolean includeLog, MineStoreCommon plugin) {
        File logFile = plugin.jarFile().getParentFile();
        while (!logFile.getAbsolutePath().endsWith("plugins") && !logFile.getAbsolutePath().endsWith("mods")) {
            logFile = logFile.getParentFile();
        }
        logFile = new File(logFile.getParentFile(), "logs" + File.separator + "latest.log");
        if (logFile.exists() && includeLog) {
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
        if (includeLog && !logFile.exists()) {
            return dump("Log file not found at " + logFile.getAbsolutePath(), plugin);
        }
        return dump("Log file not included", plugin);
    }
}
