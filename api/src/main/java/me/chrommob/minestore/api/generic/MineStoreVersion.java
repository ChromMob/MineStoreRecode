package me.chrommob.minestore.api.generic;

import me.chrommob.minestore.api.Registries;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MineStoreVersion {
    private final int major;
    private final int minor;
    private final int patch;

    public enum Comparison {
        EQUAL,
        GREATER,
        LESS
    }
    public MineStoreVersion(String version) {
        String[] split = version.split("\\.");
        this.major = Integer.parseInt(split[0]);
        this.minor = Integer.parseInt(split[1]);
        this.patch = Integer.parseInt(split[2]);
    }

    public MineStoreVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public Comparison compare(MineStoreVersion version) {
        if (this.major > version.major) {
            return Comparison.GREATER;
        } else if (this.major < version.major) {
            return Comparison.LESS;
        } else {
            if (this.minor > version.minor) {
                return Comparison.GREATER;
            } else if (this.minor < version.minor) {
                return Comparison.LESS;
            } else {
                if (this.patch > version.patch) {
                    return Comparison.GREATER;
                } else if (this.patch < version.patch) {
                    return Comparison.LESS;
                } else {
                    return Comparison.EQUAL;
                }
            }
        }
    }

    public boolean requires(MineStoreVersion version) {
        return this.compare(version) == Comparison.GREATER || this.compare(version) == Comparison.EQUAL;
    }

    public Comparison compare(String version) {
        return this.compare(new MineStoreVersion(version));
    }

    public boolean requires(String version) {
        return this.requires(new MineStoreVersion(version));
    }

    public Comparison compare(int major, int minor, int patch) {
        return this.compare(new MineStoreVersion(major, minor, patch));
    }

    public boolean requires(int major, int minor, int patch) {
        return this.requires(new MineStoreVersion(major, minor, patch));
    }

    @Override
    public String toString() {
        return this.major + "." + this.minor + "." + this.patch;
    }

    private static final MineStoreVersion dummy = new MineStoreVersion(0, 0, 0);
    public static MineStoreVersion dummy() {
        return dummy;
    }

    public static MineStoreVersion getMineStoreVersion(String storeUrl) {
        if (storeUrl.endsWith("/")) {
            storeUrl = storeUrl.substring(0, storeUrl.length() - 1);
        }
        storeUrl = storeUrl + "/api/getVersion";
        try {
            HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(storeUrl).openConnection();
            InputStream in = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(in));
            String line;
            if ((line = reader.readLine()) != null) {
                String[] split = line.split("\\.");
                try {
                    Integer.parseInt(split[0]);
                } catch (NumberFormatException e) {
                    return MineStoreVersion.dummy();
                }
                return new MineStoreVersion(line);
            }
        } catch (IOException e) {
            Registries.LOGGER.get().log("Failed to get MineStore version!");
            Registries.LOGGER.get().log(e.getMessage());
        }
        return MineStoreVersion.dummy();
    }
}
