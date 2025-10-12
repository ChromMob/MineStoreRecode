package me.chrommob.minestore.common.config.lang;

import me.chrommob.minestore.libs.me.chrommob.config.ConfigManager.ConfigKey;
import me.chrommob.minestore.libs.me.chrommob.config.ConfigManager.ConfigWrapper;

import java.util.ArrayList;
import java.util.List;

public class cs_CZ extends ConfigWrapper {
    public cs_CZ() {
        super("cs_CZ", getKeys());
    }

    private static List<ConfigKey<?>> getKeys() {
        List<ConfigKey<?>> keys = new ArrayList<>();

        List<ConfigKey<String>> authKeys = new ArrayList<>();
        authKeys.add(new ConfigKey<>("initial-message", "<dark_green>Pokousite se prihlasit do naseho obchodu. <click:run_command:/ms auth><bold><gold>KLIKNETE ZDE</gold></bold></click> abyste potvrdily prihlaseni! Pokud vam nejde kliknout napiste do chatu /ms auth"));
        authKeys.add(new ConfigKey<>("success-message", "<dark_green>Uspesne jsi se prihlasil!</dark_green>"));
        authKeys.add(new ConfigKey<>("failure-message", "<dark_red>Nemas zadnou zadost o prihlaseni!</dark_red>"));
        authKeys.add(new ConfigKey<>("timeout-message", "<dark_red>Zadost o prihlaseni vyprsela!</dark_red>"));
        keys.add(new ConfigKey<>("auth", authKeys));

        List<ConfigKey<String>> storeCommandKeys = new ArrayList<>();
        storeCommandKeys.add(new ConfigKey<>("message", "<dark_green>Navstiv nas obchod <hover:show_text:'<gold>Klikni pro otevreni obchodu!'><click:open_url:%store_url%><bold><gold>zde</gold></bold></click></hover>!</dark_green>"));
        keys.add(new ConfigKey<>("store-command", storeCommandKeys));

        List<ConfigKey<?>> buyGuiKeys = new ArrayList<>();

        buyGuiKeys.add(new ConfigKey<>("message", "<dark_green>Pro zakoupeni <red><bold>%package%</bold></red> klikni <click:open_url:%buy_url%><bold><gold>SEM</gold></bold></click>!"));

        List<ConfigKey<String>> buyGuiBackItemKeys = new ArrayList<>();
        buyGuiBackItemKeys.add(new ConfigKey<>("name", "<red>Zpet"));
        buyGuiBackItemKeys.add(new ConfigKey<>("description", "<red>Vrat se do posledniho menu!"));
        buyGuiKeys.add(new ConfigKey<>("back", buyGuiBackItemKeys));

        List<ConfigKey<String>> buyGuiCategoryKeys = new ArrayList<>();
        buyGuiCategoryKeys.add(new ConfigKey<>("title", "<bold><blue>OBCHOD"));
        buyGuiCategoryKeys.add(new ConfigKey<>("name", "<gold>%category%"));
        buyGuiKeys.add(new ConfigKey<>("category", buyGuiCategoryKeys));

        List<ConfigKey<String>> buyGuiSubCategoryKeys = new ArrayList<>();
        buyGuiSubCategoryKeys.add(new ConfigKey<>("title", "<red><bold>%category%"));
        buyGuiSubCategoryKeys.add(new ConfigKey<>("name", "<gold>%subcategory%"));
        buyGuiKeys.add(new ConfigKey<>("subcategory", buyGuiSubCategoryKeys));

        List<ConfigKey<?>> buyGuiPackageKeys = new ArrayList<>();
        buyGuiPackageKeys.add(new ConfigKey<>("title", "<red><bold>%subcategory%"));
        buyGuiPackageKeys.add(new ConfigKey<>("name", "<gold>%package%"));
        buyGuiPackageKeys.add(new ConfigKey<>("description", "<white>%description%"));
        List<ConfigKey<String>> buyGuiPackagePriceKeys = new ArrayList<>();
        buyGuiPackagePriceKeys.add(new ConfigKey<>("normal", "<green>Cena: </green><gold>%price%CZK</gold>"));
        buyGuiPackagePriceKeys.add(new ConfigKey<>("virtual", "<green>Cena: </green><gold>%price%QQ</gold>"));
        buyGuiPackageKeys.add(new ConfigKey<>("price", buyGuiPackagePriceKeys));
        buyGuiKeys.add(new ConfigKey<>("package", buyGuiPackageKeys));

        keys.add(new ConfigKey<>("buy-gui", buyGuiKeys));

        List<ConfigKey<String>> subscriptionKeys = new ArrayList<>();
        subscriptionKeys.add(new ConfigKey<>("title", "<red><bold>Odebírání:</red>"));
        subscriptionKeys.add(new ConfigKey<>("status", "<dark_green>%message%</dark_green>"));
        subscriptionKeys.add(new ConfigKey<>("url", "<click:open_url:%url%><yellow>%url%</yellow></click>"));
        keys.add(new ConfigKey<>("subscription", subscriptionKeys));

        return keys;
    }
}
