package me.chrommob.minestore.common.config;

import me.chrommob.minestore.common.config.lang.cs_CZ;
import me.chrommob.minestore.common.config.lang.en_US;
import me.chrommob.minestore.libs.me.chrommob.config.ConfigManager.ConfigKey;
import me.chrommob.minestore.libs.me.chrommob.config.ConfigManager.ConfigWrapper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.function.Function;

public final class ConfigKeys {
    private ConfigKeys() {}

    public static final ConfigKey<Boolean> DEBUG = new ConfigKey<>("debug", false, Collections.singletonList("Only enable this if you are asked to by the MineStore developer."));
    public static final ConfigKey<Boolean> COMMAND_EXEC_LOGGING = new ConfigKey<>("command-execution-logging", true, Collections.singletonList("If this is enabled every command executed by minestore will be logged to the console."));

    private static final Function<String, String> URL_ENCODER = s -> {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    };

    private static final Function<String, String> URL_DECODER = s -> {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    };

    public static final ConfigKey<String> LANG;
    public static final ConfigKey<String> STORE_URL;

    private static final Map<String, ConfigWrapper> langMap = new HashMap<>();
    private static final Function<String, String> URL_STANDARDIZER = s -> {
        if (s.endsWith("/")) {
            return s;
        } else {
            return s + "/";
        }
    };

    static {
        langMap.put("cs_CZ", new cs_CZ());
        langMap.put("en_US", new en_US());
        List<String> langComment = new ArrayList<>();
        langComment.add("Set the language that is used by the plugin.");
        langComment.add("Available languages: " + langMap.keySet() + " but you can create your own language file.");
        langComment.add("To create your own language file, just set language to whatever you want and the plugin will create template file for you in the lang folder.");
        LANG = new ConfigKey<>("language", "en_US", langComment);

        List<String> storeUrlComment = new ArrayList<>();
        storeUrlComment.add("Set the store URL that is used by the plugin.");
        storeUrlComment.add("The URL must start with https:// and end with /");

        STORE_URL = new ConfigKey<>("store-url", "https://store.example.com/", storeUrlComment, null, URL_STANDARDIZER);
    }

    public static final class API_KEYS {
        private API_KEYS() {}

        public static final ConfigKey<Boolean> ENABLED = new ConfigKey<>("key-enabled", false, Collections.singletonList("API key is not required only on very old versions of MineStore, so enable this."));
        public static final ConfigKey<String> KEY = new ConfigKey<>("key", "123456789", Collections.singletonList("The API key that is used by the plugin."), URL_DECODER, URL_ENCODER);
    }

    public static final class WEBLISTENER_KEYS {
        private WEBLISTENER_KEYS() {}

        public static final ConfigKey<Boolean> ENABLED = new ConfigKey<>("secret-enabled", false, Collections.singletonList("Secret key is not required only on very old versions of MineStore, so enable this."));
        public static final ConfigKey<String> KEY = new ConfigKey<>("secret-key", "extraSecretKey", Collections.singletonList("The secret key that is used to authenticate the getting of commands."), URL_DECODER, URL_ENCODER);
    }

    public static final class PAYNOW_KEYS {
        private PAYNOW_KEYS() {}

        public static final ConfigKey<Boolean> SHARE_IP_ON_JOIN;

        static {
            List<String> onJoinComment = new ArrayList<>();
            onJoinComment.add("By providing PayNow with IP data, you enable enhanced fraud detection and improve your chances of winning chargeback disputes.");
            onJoinComment.add("If this feature is disabled, you may experience more chargeback losses, and PayNow may revoke your chargeback protection.");
            SHARE_IP_ON_JOIN = new ConfigKey<>("share-ip-onjoin", true, onJoinComment);
        }
    }

    public static final class AUTH_KEYS {
        private AUTH_KEYS() {}

        public static final ConfigKey<Integer> TIMEOUT;

        static {
            List<String> authTimeoutComment = new ArrayList<>();
            authTimeoutComment.add("Set the timeout in seconds that the player has to authenticate their Minecraft account with MineStore website.");
            authTimeoutComment.add("This is the amount of time in seconds that the player has to authenticate their Minecraft account with MineStore website.");
            TIMEOUT = new ConfigKey<>("timeout", 300, authTimeoutComment);
        }
    }

    public static final class STORE_COMMAND_KEYS {
        private STORE_COMMAND_KEYS() {}

        public static final ConfigKey<Boolean> ENABLED = new ConfigKey<>("enabled", false, Collections.singletonList("If this is enabled the player will be sent a link to the MineStore website when they run the /store command."));
    }

    public static final class BUY_GUI_KEYS {
        private BUY_GUI_KEYS() {}

        public static final ConfigKey<Boolean> ENABLED = new ConfigKey<>("enabled", false, Collections.singletonList("If this is enabled the player will be able to see packages in /buy command."));

        public static final class BACK_KEYS {
            private BACK_KEYS() {}

            public static final ConfigKey<Boolean> ENABLED = new ConfigKey<>("enabled", false);
            public static final ConfigKey<String> ITEM = new ConfigKey<>("item", "BARRIER", Collections.singletonList("The item that is used as the back item of the GUI."));
        }

        public static final class CATEGORY_KEYS {
            private CATEGORY_KEYS() {}

            public static final ConfigKey<Boolean> ENABLED = new ConfigKey<>("enabled", true, Collections.singletonList("If this is enabled the background of the GUI will be enabled."));
            public static final ConfigKey<String> ITEM = new ConfigKey<>("item", "GLASS_PANE", Collections.singletonList("The item that is used as the background of the GUI."));
        }
    }

    public static final class MYSQL_KEYS {
        private MYSQL_KEYS() {}

        public static final ConfigKey<Boolean> ENABLED = new ConfigKey<>("enabled", false, Collections.singletonList("If this is enabled the plugin will use MySQL database to sync data with MineStore website."));
        public static final ConfigKey<String> IP = new ConfigKey<>("ip", "localhost", Collections.singletonList("The IP of the MySQL database."));
        public static final ConfigKey<Integer> PORT = new ConfigKey<>("port", 3306, Collections.singletonList("The port of the MySQL database."));
        public static final ConfigKey<String> DATABASE = new ConfigKey<>("database", "minestore", Collections.singletonList("The name of the MySQL database."));
        public static final ConfigKey<String> USERNAME = new ConfigKey<>("username", "root", Collections.singletonList("The username of the MySQL database."));
        public static final ConfigKey<String> PASSWORD = new ConfigKey<>("password", "superSecretPassword", Collections.singletonList("The password of the MySQL database."));
    }
}
