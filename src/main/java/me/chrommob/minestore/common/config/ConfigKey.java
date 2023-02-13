package me.chrommob.minestore.common.config;

public enum ConfigKey
{
    DEBUG(new Configuration("debug", false)),
    STORE_URL(new Configuration("store-url", "https://store.example.com")),
    SECRET_ENABLED(new Configuration("weblistener.secret-enabled", false)),
    SECRET_KEY(new Configuration("weblistener.secret-key", 123456789)),
    AUTH_TIMEOUT(new Configuration("auth.timeout", 300)),
    AUTH_INIT_MESSAGE(new Configuration("auth.initial-message", "<dark_green>You are trying to login in to our store. <click:run_command:/ms auth><bold><gold>CLICK HERE</gold></bold></click> to confirm authorization! If you are not able to click run /minestore auth.")),
    AUTH_SUCCESS_MESSAGE(new Configuration("auth.success-message", "<dark_green>You have successfully logged in to our store!</dark_green>")),
    AUTH_FAILURE_MESSAGE(new Configuration("auth.failure-message", "<dark_red>You do not have pending auth!</dark_red>")),
    AUTH_TIMEOUT_MESSAGE(new Configuration("auth.timeout-message", "<dark_red>You have failed to log in to our store!</dark_red>")),
    STORE_COMMAND(new Configuration("store-command.enabled", false)),
    STORE_COMMAND_MESSAGE(new Configuration("store-command.message", "<dark_green>Visit our store <click:open_url:%store_url%><hover:show_text:'<gold>Click to open the store!'><bold><gold>here</gold></bold></hover></click>!</dark_green>"));

    private final Configuration configuration;

    ConfigKey(final Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
