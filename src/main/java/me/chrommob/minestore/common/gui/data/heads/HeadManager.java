package me.chrommob.minestore.common.gui.data.heads;

import me.chrommob.minestore.common.MineStoreCommon;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

public class HeadManager {
    private final HashMap<String, String> heads = new HashMap<>();
    public HeadManager(MineStoreCommon plugin) {
        String base64 = Base64.getEncoder().encodeToString(("{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/d5c6dc2bbf51c36cfc7714585a6a5683ef2b14d47d8ff714654a893f5da622\"}}}").getBytes(StandardCharsets.UTF_8));
        heads.put("default", base64);
        loadHeads(plugin);
    }

    public String getHead(String name) {
        MineStoreCommon.getInstance().log("Getting head " + name);
        if (name == null || name.isEmpty()) {
            return heads.get("default");
        }
        name = name.toLowerCase();
        name = name.replace("minecraft:", "");
        String head;
        head = heads.get(name);
        if (head == null) {
            name = name.replace("_", " ");
        }
        head = heads.get(name);
        if (head == null) {
            name = name.replace(" ", "_");
        }
        head = heads.get(name);
        //If the head is still null, return the default head (chest)
        MineStoreCommon.getInstance().log("Head " + name + " not found.");
        return head == null ? heads.get("default") : head;
    }

    private void loadHeads(MineStoreCommon plugin) {
        InputStream is = plugin.getClass().getClassLoader().getResourceAsStream("heads.csv");
        if (is != null) {
            BufferedReader db = new BufferedReader(new InputStreamReader(is));
            db.lines().forEach(line -> {
                String[] split = line.split(";");
                String base64 = Base64.getEncoder().encodeToString(("{\"textures\":{\"SKIN\":{\"url\":\"" + split[3] + "\"}}}").getBytes(StandardCharsets.UTF_8));
                //Replace all brackets with nothing
                String name = split[2].toLowerCase().replaceAll(" " , "_");
                heads.put(name.toLowerCase(), base64);
            });
        }
        plugin.log("Loaded " + heads.size() + " heads.");
    }
}
