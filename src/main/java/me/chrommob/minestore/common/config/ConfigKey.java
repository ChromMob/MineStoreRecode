package me.chrommob.minestore.common.config;

public enum ConfigKey
{
    DEBUG(new Configuration("debug", false)),

    STORE_URL(new Configuration("store-url", "https://store.example.com")),
    API_ENABLED(new Configuration("api.enabled", false)),
    API_KEY(new Configuration("api.key", "123456789")),
    SECRET_ENABLED(new Configuration("weblistener.secret-enabled", false)),
    SECRET_KEY(new Configuration("weblistener.secret-key", 123456789)),

    AUTH_TIMEOUT(new Configuration("auth.timeout", 300)),
    AUTH_INIT_MESSAGE(new Configuration("auth.initial-message", "<dark_green>You are trying to login in to our store. <click:run_command:/ms auth><bold><gold>CLICK HERE</gold></bold></click> to confirm authorization! If you are not able to click run /minestore auth.")),
    AUTH_SUCCESS_MESSAGE(new Configuration("auth.success-message", "<dark_green>You have successfully logged in to our store!</dark_green>")),
    AUTH_FAILURE_MESSAGE(new Configuration("auth.failure-message", "<dark_red>You do not have pending auth!</dark_red>")),
    AUTH_TIMEOUT_MESSAGE(new Configuration("auth.timeout-message", "<dark_red>You have failed to log in to our store!</dark_red>")),

    STORE_ENABLED(new Configuration("store-command.enabled", false)),
    STORE_COMMAND_MESSAGE(new Configuration("store-command.message", "<dark_green>Visit our store <click:open_url:%store_url%><hover:show_text:'<gold>Click to open the store!'><bold><gold>here</gold></bold></hover></click>!</dark_green>")),

    BUY_GUI_ENABLED(new Configuration("buy-gui.enabled", false)),
    BUY_GUI_BACK_ITEM(new Configuration("buy-gui.back.item", "BARRIER")),
    BUY_GUI_BACK_ITEM_NAME(new Configuration("buy-gui.back.name", "<red>Back")),
    BUY_GUI_BACK_ITEM_LORE(new Configuration("buy-gui.back.description", "<red>Go back to the previous menu!")),
    BUY_GUI_CATEGORY_TITLE(new Configuration("buy-gui.category.title", "<red><bold>Categories")),
    BUY_GUI_CATEGORY_NAME(new Configuration("buy-gui.category.name", "<gold>%category%")),
    BUY_GUI_SUBCATEGORY_TITLE(new Configuration("buy-gui.subcategory.title", "<red><bold>%category%")),
    BUY_GUI_SUBCATEGORY_NAME(new Configuration("buy-gui.subcategory.name", "<gold>%subcategory%")),
    BUY_GUI_PACKAGE_TITLE(new Configuration("buy-gui.package.title", "<red><bold>%subcategory%")),
    BUY_GUI_PACKAGE_NAME(new Configuration("buy-gui.package.name", "<gold>%package%")),
    BUY_GUI_PACKAGE_LORE(new Configuration("buy-gui.package.description", "<white>%description%")),
    BUY_GUI_PACKAGE_PRICE(new Configuration("buy-gui.package.price", "<green>Price: </green><gold>%price%USD</gold>")),
    BUY_GUI_MESSAGE(new Configuration("buy-gui.message", "<dark_green>To buy <red><bold>%package%</bold></red> click <click:open_url:%buy_url%><bold><gold>HERE</gold></bold></click>!")),

    MYSQL_ENABLED(new Configuration("mysql.enabled", false)),
    MYSQL_HOST(new Configuration("mysql.ip", "localhost")),
    MYSQL_PORT(new Configuration("mysql.port", 3306)),
    MYSQL_DATABASE(new Configuration("mysql.database", "minestore")),
    MYSQL_USERNAME(new Configuration("mysql.username", "root")),
    MYSQL_PASSWORD(new Configuration("mysql.password", "password"));


    private final Configuration configuration;

    ConfigKey(final Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
