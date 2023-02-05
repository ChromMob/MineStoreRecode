package me.chrommob.minestore.common.config;


public enum ConfigKey {
    DEBUG(new Configuration("debug", false)),
    STORE_URL(new Configuration("store-url", "https://store.example.com")),
    SECRET_ENABLED(new Configuration("weblistener.secret-enabled", false)),
    SECRET_KEY(new Configuration("weblistener.secret-key", 123456789)),
    AUTH_TIMEOUT(new Configuration("auth.timeout", 300)),
    AUTH_INIT_MESSAGE(new Configuration("auth.initial-message", "<dark_green>You are trying to log in to our </dark_green><gold><bold>store. CLICK HERE </bold><dark_green>to confirm authorization! If you are not able to click run /minestore auth.</dark_green>")),
    AUTH_SUCCESS_MESSAGE(new Configuration("auth.success-message", "<dark_green>You have successfully logged in to our store!</dark_green>")),
    AUTH_FAILURE_MESSAGE(new Configuration("auth.failure-message", "<dark_red>You have failed to log in to our store!</dark_red>"));

    private final Configuration configuration;
    ConfigKey(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
