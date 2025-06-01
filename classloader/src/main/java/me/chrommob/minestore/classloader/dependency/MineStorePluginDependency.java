package me.chrommob.minestore.classloader.dependency;

import me.chrommob.minestore.classloader.repository.MineStorePluginRepository;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MineStorePluginDependency {
    private final String group;
    private final String name;
    private final String version;
    private final Map<String, String> relocations;

    public MineStorePluginDependency(String group, String name, String version) {
        this(group, name, version, new HashMap<>());
    }

    public MineStorePluginDependency(String group, String name, String version, Map<String, String> relocations) {
        this.group = group;
        this.name = name;
        this.version = version;
        this.relocations = relocations;
    }

    public static MineStorePluginDependency fromGradle(String gradle) {
        int start = gradle.indexOf('"');
        if (start == -1) {
            start = gradle.indexOf('\'');
        }
        int end = gradle.lastIndexOf('"');
        if (end == -1) {
            end = gradle.lastIndexOf('\'');
        }
        String dependency;
        if (start != -1 && end != -1) {
            dependency = gradle.substring(start + 1, end);
        } else {
            dependency = gradle;
        }
        String[] split = dependency.split(":");
        return new MineStorePluginDependency(split[0], split[1], split[2]);
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public boolean conflictsWith(MineStorePluginDependency dependency) {
        return group.equals(dependency.group) && name.equals(dependency.name) && !version.equals(dependency.version);
    }

    public Optional<byte[]> download(MineStorePluginRepository repository) {
        String path = repository.getUrl() + "/" + group.replace('.', '/') + "/" + name + "/" + version + "/" + name + "-" + version + ".jar";
        try {
            URL url = new URL(path);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("User-Agent", "MineStore");
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            try (InputStream in = urlConnection.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                byte[] byteStreams = out.toByteArray();
                if (byteStreams.length == 0) {
                    System.out.println("Could not download " + path);
                    return Optional.empty();
                }
                return Optional.of(byteStreams);
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<String> getFromURL(String url) {
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "MineStore");
            connection.setDoOutput(true);
            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return Optional.of(builder.toString());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public String getSha(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] byteArray = new byte[1024];
                int bytesCount;

                while ((bytesCount = fis.read(byteArray)) != -1) {
                    digest.update(byteArray, 0, bytesCount);
                }
            }

            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();

            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public String getMd5(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] byteArray = new byte[1024];
                int bytesCount;

                while ((bytesCount = fis.read(byteArray)) != -1) {
                    digest.update(byteArray, 0, bytesCount);
                }
            }

            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();

            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean verify(File file, MineStorePluginRepository repository) {
        String sha = repository.getUrl() + "/" + group.replace('.', '/') + "/" + name + "/" + version + "/" + name + "-" + version + ".jar.sha1";
        String md5 = repository.getUrl() + "/" + group.replace('.', '/') + "/" + name + "/" + version + "/" + name + "-" + version + ".jar.md5";
        Optional<String> sha1 = getFromURL(sha);
        Optional<String> md52 = getFromURL(md5);
        if (!sha1.isPresent() || !md52.isPresent()) {
            return false;
        }
        if (!sha1.get().equals(getSha(file))) {
            return false;
        }
        return md52.get().equals(getMd5(file));
    }

    public void addRelocation(String from, String to) {
        relocations.put(from, to);
    }

    public Map<String, String> getRelocations() {
        return relocations;
    }

    public boolean hasRelocations() {
        return !relocations.isEmpty();
    }
}
