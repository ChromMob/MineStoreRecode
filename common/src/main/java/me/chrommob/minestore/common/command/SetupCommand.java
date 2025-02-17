package me.chrommob.minestore.common.command;

import me.chrommob.config.ConfigKey;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.common.config.PluginConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;

import java.util.*;

@SuppressWarnings("unused")
public class SetupCommand {
    private final MineStoreCommon plugin;
    public SetupCommand(MineStoreCommon plugin) {
        this.plugin = plugin;
    }

    @Permission("minestore.setup")
    @Command("minestore|ms setup <configKey> [value]")
    public void onSetupCommand(AbstractUser user, @Argument(value = "configKey", suggestions = "configKeys") String key, @Argument("value") String value) {
        CommonUser commonUser = user.user();
        if (keys == null) {
             getAllNames();
        }
        ConfigKey config = keys.get(key);
        if (config == null) {
            commonUser.sendMessage(Component.text("Config key " + key + " does not exist").color(NamedTextColor.RED));
            return;
        }
        if (value == null) {
            commonUser.sendMessage(Component.text("The value of " + key + " is " + config.getAsString()).color(NamedTextColor.GREEN));
            return;
        }
        config.setValue(value);
        plugin.pluginConfig().saveConfig();
        plugin.reload();
        commonUser.sendMessage(Component.text("Value of " + key + " has been set to " + value).color(NamedTextColor.GREEN));
    }

    @Suggestions("configKeys")
    public Set<String> suggestions(CommandContext<AbstractUser> context, CommandInput input) {
        return new HashSet<>(getAllNames());
    }

    private Map<String, ConfigKey> keys;
    private Set<String> getAllNames() {
        if (keys != null) return keys.keySet();
        keys =  new HashMap<>();
        for (ConfigKey key : PluginConfig.getKeys()) {
            ConfigKey real = plugin.pluginConfig().getKey(key.get());
            keys.putAll(getNames(new StringBuilder(), real));
        }
        return keys.keySet();
    }

    private Map<String, ConfigKey> getNames(StringBuilder stringBuilder, ConfigKey key) {
        Map<String, ConfigKey> names = new HashMap<>();
        if (key.getChildren().isEmpty()) {
            names.put(stringBuilder.toString() + key.get().toUpperCase(), key);
        } else {
            stringBuilder.append(key.get().toUpperCase()).append(".");
            for (ConfigKey child : key.getChildren().values()) {
                names.putAll(getNames(stringBuilder, child));
            }
        }
        return names;
    }
}
