package me.chrommob.minestore.common.command;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.interfaces.user.CommonUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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
        if (key == null) {
            commonUser.sendMessage(Component.text("Config keys: ").color(NamedTextColor.GRAY).append(Component.text(Arrays.stream(ConfigKey.values()).map(ConfigKey::name).map(String::toUpperCase).collect(Collectors.joining(", "))).color(NamedTextColor.WHITE)).append(Component.text(" | ").color(NamedTextColor.GRAY)).append(Component.text("CHANGE VALUE: /minestore setup <configKey> <value>").color(NamedTextColor.RED)).decorate(TextDecoration.BOLD));
            return;
        }
        ConfigKey configKey;
        try {
            configKey = ConfigKey.valueOf(key.toUpperCase());
        } catch (IllegalArgumentException e) {
            commonUser.sendMessage(Component.text("Invalid key!").color(NamedTextColor.RED));
            return;
        }
        if (value == null) {
            commonUser.sendMessage(Component.text("Current value: ").color(NamedTextColor.GRAY).append(Component.text(plugin.configReader().get(configKey).toString()).color(NamedTextColor.WHITE)).append(Component.text(" | ").color(NamedTextColor.GRAY)).append(Component.text("CHANGE VALUE: /minestore setup " + key.toUpperCase() + " <value>").color(NamedTextColor.RED)).decorate(TextDecoration.BOLD));
            return;
        }
        //Cast value to the correct type
        Class<?> type = configKey.getConfiguration().getDefaultValue().getClass();
        try {
            if (type == String.class) {
                plugin.configReader().set(configKey, value);
            } else if (type == Integer.class) {
                plugin.configReader().set(configKey, Integer.parseInt(value));
            } else if (type == Boolean.class) {
                plugin.configReader().set(configKey, Boolean.parseBoolean(value));
            } else if (type == Double.class) {
                plugin.configReader().set(configKey, Double.parseDouble(value));
            } else if (type == Float.class) {
                plugin.configReader().set(configKey, Float.parseFloat(value));
            } else if (type == Long.class) {
                plugin.configReader().set(configKey, Long.parseLong(value));
            } else if (type == Short.class) {
                plugin.configReader().set(configKey, Short.parseShort(value));
            } else if (type == Byte.class) {
                plugin.configReader().set(configKey, Byte.parseByte(value));
            } else if (type == Character.class) {
                plugin.configReader().set(configKey, value.charAt(0));
            } else {
                commonUser.sendMessage(Component.text("Value is not a valid type!").color(NamedTextColor.RED));
                return;
            }
        } catch (Exception e) {
            commonUser.sendMessage(Component.text("Invalid value!").color(NamedTextColor.RED));
            return;
        }
        commonUser.sendMessage(Component.text("Successfully set ").color(NamedTextColor.GREEN).append(Component.text(key.toUpperCase()).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD)).append(Component.text(" to ").color(NamedTextColor.GREEN)).append(Component.text(value).color(NamedTextColor.WHITE).decorate(TextDecoration.BOLD)));
        plugin.reload();
    }

    @Suggestions("configKeys")
    public Set<String> suggestions(CommandContext<AbstractUser> context, CommandInput input) {
        Set<String> keys = new HashSet<>();
        for (ConfigKey key : ConfigKey.values()) {
            keys.add(key.name().toUpperCase());
        }
        return keys;
    }

}
