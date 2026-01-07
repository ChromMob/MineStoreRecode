package me.chrommob.minestore.common.dumper;

import com.google.gson.Gson;
import me.chrommob.minestore.api.web.Result;
import me.chrommob.minestore.api.web.WebContext;
import me.chrommob.minestore.api.web.WebRequest;
import me.chrommob.minestore.common.MineStoreCommon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Dumper {
    private final Gson gson = new Gson();

    public String dump(String log, MineStoreCommon plugin) {
        String dumpLink = "https://paste.chrommob.fun/";
        WebRequest<String> request = new WebRequest.Builder<>(String.class).customUrl("https://paste.chrommob.fun/post").type(WebRequest.Type.POST).strBody(gson.toJson(new DumpData(log, plugin))).build();
        Result<String, WebContext> res = plugin.apiHandler().request(request);
        if (res.isError()) {
            plugin.log("Failed to dump log");
            plugin.debug(this.getClass(), res.context());
            return null;
        }

        if (res.context().responseCode() == 201) {
            return dumpLink + res.context().responseString();
        } else {
            if (res.context().responseCode() == 413) {
                return dump(false, plugin);
            }
            return res.context().responseString() + " (" + res.context().responseCode() + ")";
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
