package me.chrommob.minestore.common.config.lang;

import me.chrommob.config.ConfigKey;
import me.chrommob.config.ConfigWrapper;

import java.util.ArrayList;
import java.util.List;

public class en_US extends ConfigWrapper {
    public en_US(String name) {
        super(name, getKeys());
    }

    public en_US() {
        super("en_US", getKeys());
    }

    private static List<ConfigKey> getKeys() {
        List<ConfigKey> keys = new ArrayList<>();

        List<ConfigKey> authKeys = new ArrayList<>();
        authKeys.add(new ConfigKey("initial-message", "<dark_green>You are trying to login in to our store. <click:run_command:/ms auth><bold><gold>CLICK HERE</gold></bold></click> to confirm authorization! If you are not able to click run /minestore auth."));
        authKeys.add(new ConfigKey("success-message", "<dark_green>You have successfully logged in to our store!</dark_green>"));
        authKeys.add(new ConfigKey("failure-message", "<dark_red>You do not have pending auth!</dark_red>"));
        authKeys.add(new ConfigKey("timeout-message", "<dark_red>You have failed to log in to our store!</dark_red>"));
        keys.add(new ConfigKey("auth", authKeys));

        List<ConfigKey> storeCommandKeys = new ArrayList<>();
        storeCommandKeys.add(new ConfigKey("message", "<dark_green>Visit our store <click:open_url:%store_url%><hover:show_text:'<gold>Click to open the store!'><bold><gold>here</gold></bold></hover></click>!</dark_green>"));
        keys.add(new ConfigKey("store-command", storeCommandKeys));

        List<ConfigKey> buyGuiKeys = new ArrayList<>();

        buyGuiKeys.add(new ConfigKey("message", "<dark_green>To buy <red><bold>%package%</bold></red> click <click:open_url:%buy_url%><bold><gold>HERE</gold></bold></click>!"));

        List<ConfigKey> buyGuiBackItemKeys = new ArrayList<>();
        buyGuiBackItemKeys.add(new ConfigKey("name", "<red>Back"));
        buyGuiBackItemKeys.add(new ConfigKey("description", "<red>Go back to the previous menu!"));
        buyGuiKeys.add(new ConfigKey("back", buyGuiBackItemKeys));

        List<ConfigKey> buyGuiCategoryKeys = new ArrayList<>();
        buyGuiCategoryKeys.add(new ConfigKey("title", "<bold><blue>STORE"));
        buyGuiCategoryKeys.add(new ConfigKey("name", "<gold>%category%"));
        buyGuiKeys.add(new ConfigKey("category", buyGuiCategoryKeys));

        List<ConfigKey> buyGuiSubCategoryKeys = new ArrayList<>();
        buyGuiSubCategoryKeys.add(new ConfigKey("title", "<red><bold>%category%"));
        buyGuiSubCategoryKeys.add(new ConfigKey("name", "<gold>%subcategory%"));
        buyGuiKeys.add(new ConfigKey("subcategory", buyGuiSubCategoryKeys));

        List<ConfigKey> buyGuiPackageKeys = new ArrayList<>();
        buyGuiPackageKeys.add(new ConfigKey("title", "<red><bold>%subcategory%"));
        buyGuiPackageKeys.add(new ConfigKey("name", "<gold>%package%"));
        buyGuiPackageKeys.add(new ConfigKey("description", "<white>%description%"));
        List<ConfigKey> buyGuiPackagePriceKeys = new ArrayList<>();
        buyGuiPackagePriceKeys.add(new ConfigKey("normal", "<green>Price: </green><gold>%price%USD</gold>"));
        buyGuiPackagePriceKeys.add(new ConfigKey("virtual", "<green>Price: </green><gold>%price%QQ</gold>"));
        buyGuiPackageKeys.add(new ConfigKey("price", buyGuiPackagePriceKeys));
        buyGuiKeys.add(new ConfigKey("package", buyGuiPackageKeys));

        keys.add(new ConfigKey("buy-gui", buyGuiKeys));

        List<ConfigKey> subscriptionKeys = new ArrayList<>();
        subscriptionKeys.add(new ConfigKey("title", "<red><bold>Subscriptions:</red>"));
        subscriptionKeys.add(new ConfigKey("status", "<dark_green>%message%</dark_green>"));
        subscriptionKeys.add(new ConfigKey("url", "<click:open_url:%url%><yellow>%url%</yellow></click>"));
        keys.add(new ConfigKey("subscription", subscriptionKeys));

        return keys;
    }
}
