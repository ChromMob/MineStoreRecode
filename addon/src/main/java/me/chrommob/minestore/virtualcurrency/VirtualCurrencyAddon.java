package me.chrommob.minestore.virtualcurrency;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.event.MineStoreEventBus;
import me.chrommob.minestore.api.event.types.MineStoreLoadEvent;
import me.chrommob.minestore.api.event.types.MineStorePlayerJoinEvent;
import me.chrommob.minestore.api.generic.MineStoreAddon;
import me.chrommob.minestore.api.placeholder.PlaceHolderManager;
import me.chrommob.minestore.libs.me.chrommob.config.ConfigManager.ConfigKey;
import me.chrommob.minestore.virtualcurrency.commands.*;
import me.chrommob.minestore.virtualcurrency.database.DatabaseManager;
import me.chrommob.minestore.virtualcurrency.events.PlayerEventHandler;
import me.chrommob.minestore.virtualcurrency.placeholders.VirtualCurrencyPlaceholders;
import me.chrommob.minestore.virtualcurrency.providers.VirtualEconomyProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings("unused")
public class VirtualCurrencyAddon extends MineStoreAddon {
    private static final Function<Component, String> serialize = c -> MiniMessage.miniMessage().serialize(c);
    private static final Function<String, Component> deserialize = s -> MiniMessage.miniMessage().deserialize(s);

    private static DatabaseManager databaseManager;
    private static VirtualEconomyProvider economyProvider;

    private static final ConfigKey<Component> ENABLE_MESSAGE;
    private static final ConfigKey<String> CURRENCY_NAME;
    private static final ConfigKey<String> CURRENCY_SYMBOL;
    private static final ConfigKey<Double> DEFAULT_BALANCE;
    private static final ConfigKey<Integer> BALANCE_PRECISION;
    private static final ConfigKey<Integer> TOP_LIMIT;
    private static final ConfigKey<Boolean> DAILY_BONUS_ENABLED;
    private static final ConfigKey<Double> DAILY_BONUS_AMOUNT;
    private static final ConfigKey<Boolean> PAY_ENABLED;

    static {
        Component enableComp = Component.text("VirtualCurrency").decorate(TextDecoration.BOLD).color(NamedTextColor.GOLD)
                .append(Component.text(" enabled!")).color(NamedTextColor.GREEN);
        ENABLE_MESSAGE = new ConfigKey<Component>("enable_message", enableComp, serialize, deserialize);
        CURRENCY_NAME = new ConfigKey<>("currency_name", "Gems");
        CURRENCY_SYMBOL = new ConfigKey<>("currency_symbol", "💎");
        DEFAULT_BALANCE = new ConfigKey<>("default_balance", 0.0);
        BALANCE_PRECISION = new ConfigKey<>("balance_precision", 0);
        TOP_LIMIT = new ConfigKey<>("top_limit", 10);
        DAILY_BONUS_ENABLED = new ConfigKey<>("daily_bonus_enabled", true);
        DAILY_BONUS_AMOUNT = new ConfigKey<>("daily_bonus_amount", 10.0);
        PAY_ENABLED = new ConfigKey<>("pay_enabled", true);
    }

    @Override
    public void onEnable() {
        File dataFolder = getApiData().getDataFolder();

        databaseManager = new DatabaseManager(new File(dataFolder, "currency.db"));
        databaseManager.initialize();

        economyProvider = new VirtualEconomyProvider();

        Registries.PLAYER_ECONOMY_PROVIDER.set(economyProvider);

        registerEventListeners();
        registerPlaceholders();

        String message = PlainTextComponentSerializer.plainText().serialize(ENABLE_MESSAGE.getValue());
        Registries.LOGGER.get().log(message);
    }

    private void registerEventListeners() {
        MineStoreEventBus.registerListener(this, MineStoreLoadEvent.class, event -> databaseManager.initialize());
        MineStoreEventBus.registerListener(this, MineStorePlayerJoinEvent.class, event -> new PlayerEventHandler().handle(event));
    }

    private void registerPlaceholders() {
        PlaceHolderManager.getInstance().registerPlaceHolder("currency_balance", VirtualCurrencyPlaceholders::getBalance);
        PlaceHolderManager.getInstance().registerPlaceHolder("currency_name", VirtualCurrencyPlaceholders::getCurrencyName);
        PlaceHolderManager.getInstance().registerPlaceHolder("currency_rank", VirtualCurrencyPlaceholders::getRank);
        PlaceHolderManager.getInstance().registerPlaceHolder("currency_pay", VirtualCurrencyPlaceholders::isPayEnabled);
    }

    public String getName() {
        return "MineStore-VirtualCurrency";
    }

    @Override
    public List<Object> getCommands() {
        List<Object> commands = new ArrayList<>();
        commands.add(new BalanceCommands());
        commands.add(new PayCommand());
        commands.add(new GuiCommand());
        commands.add(new LeaderboardCommand());
        commands.add(new HistoryCommand());
        commands.add(new AdminCommand());
        return commands;
    }

    @Override
    public List<ConfigKey<?>> getConfigKeys() {
        List<ConfigKey<?>> keys = new ArrayList<>();
        keys.add(ENABLE_MESSAGE);
        keys.add(CURRENCY_NAME);
        keys.add(CURRENCY_SYMBOL);
        keys.add(DEFAULT_BALANCE);
        keys.add(BALANCE_PRECISION);
        keys.add(TOP_LIMIT);
        keys.add(DAILY_BONUS_ENABLED);
        keys.add(DAILY_BONUS_AMOUNT);
        keys.add(PAY_ENABLED);
        return keys;
    }

    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public static String getCurrencyName() {
        return CURRENCY_NAME.getValue();
    }

    public static String getCurrencySymbol() {
        return CURRENCY_SYMBOL.getValue();
    }

    public static double getDefaultBalance() {
        return DEFAULT_BALANCE.getValue();
    }

    public static int getBalancePrecision() {
        return BALANCE_PRECISION.getValue();
    }

    public static int getTopLimit() {
        return TOP_LIMIT.getValue();
    }

    public static boolean isDailyBonusEnabled() {
        return DAILY_BONUS_ENABLED.getValue();
    }

    public static double getDailyBonusAmount() {
        return DAILY_BONUS_AMOUNT.getValue();
    }

    public static boolean isPayEnabled() {
        return PAY_ENABLED.getValue();
    }
}
