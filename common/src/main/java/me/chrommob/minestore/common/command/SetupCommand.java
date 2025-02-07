package me.chrommob.minestore.common.command;

import me.chrommob.config.ConfigKey;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.common.config.PluginConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class SetupCommand {
    private final MineStoreCommon plugin;
    public SetupCommand(MineStoreCommon plugin) {
        this.plugin = plugin;
    }

    @Permission("minestore.setup")
    @Command("minestore|ms setup [configKey] [value]")
    public void onSetupCommand(AbstractUser user, @Argument(value = "configKey", suggestions = "configKeys") String key, @Argument("value")  String value) {
        CommonUser commonUser = user.user();


    }

    @Suggestions("configKeys")
    public Set<String> suggestions(CommandContext<AbstractUser> context, CommandInput input) {
        Set<String> keys = new HashSet<>();
        for (ConfigKey key : PluginConfig.getKeys()) {
            keys.addAll(getNames(key));
        }
        return keys;
    }

    private List<String> getNames(ConfigKey key) {
        List<String> names = new ArrayList<>();
        if (key.getChildren().isEmpty()) {
            names.add(key.get().toUpperCase());
        } else {
            for (ConfigKey child : key.getChildren().values()) {
                names.addAll(getNames(child));
            }
        }
        return names;
    }

}
