package me.chrommob.minestore.api;

import me.chrommob.minestore.api.interfaces.Registry;
import me.chrommob.minestore.api.interfaces.commands.CommandExecuterCommon;
import me.chrommob.minestore.api.interfaces.economyInfo.DefaultPlayerEconomyProvider;
import me.chrommob.minestore.api.interfaces.economyInfo.PlayerEconomyProvider;
import me.chrommob.minestore.api.interfaces.event.PlayerEventListener;
import me.chrommob.minestore.api.interfaces.logger.LoggerCommon;
import me.chrommob.minestore.api.interfaces.placeholder.CommonPlaceHolderProvider;
import me.chrommob.minestore.api.interfaces.playerInfo.DefaultPlayerInfoProvider;
import me.chrommob.minestore.api.interfaces.playerInfo.PlayerInfoProvider;
import me.chrommob.minestore.api.interfaces.scheduler.CommonScheduler;
import me.chrommob.minestore.api.interfaces.user.UserGetter;
import org.incendo.cloud.CommandManager;

import java.io.File;
import java.net.InetSocketAddress;

public class Registries {
    public static final Registry<String> PLATFORM = new Registry<>();
    public static final Registry<String> PLATFORM_NAME = new Registry<>();
    public static final Registry<String> PLATFORM_VERSION = new Registry<>();

    public static final Registry<InetSocketAddress> IP = new Registry<>(new InetSocketAddress(0));
    public static final Registry<String> HOSTNAME = new Registry<>("default");

    public static final Registry<LoggerCommon> LOGGER = new Registry<>();
    public static final Registry<CommonScheduler> SCHEDULER = new Registry<>();
    public static final Registry<UserGetter> USER_GETTER = new Registry<>();
    public static final Registry<CommandExecuterCommon> COMMAND_EXECUTER = new Registry<>();
    public static final Registry<PlayerInfoProvider> PLAYER_INFO_PROVIDER = new Registry<>(new DefaultPlayerInfoProvider());
    public static final Registry<PlayerEconomyProvider> PLAYER_ECONOMY_PROVIDER = new Registry<>(new DefaultPlayerEconomyProvider());
    public static final Registry<File> CONFIG_FILE = new Registry<>();
    public static final Registry<PlayerEventListener> PLAYER_JOIN_LISTENER = new Registry<>();
    public static final Registry<CommandManager<?>> COMMAND_MANAGER = new Registry<>();
    public static final Registry<CommonPlaceHolderProvider> PLACE_HOLDER_PROVIDER = new Registry<>();
}
