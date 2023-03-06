package me.chrommob.minestore.common.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.command.types.CommonConsoleUser;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.interfaces.user.CommonUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

@CommandAlias("minestore|ms")
public class SetupCommand extends BaseCommand {
    private final MineStoreCommon plugin;
    public SetupCommand(MineStoreCommon plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("unused")
    @CommandPermission("minestore.setup")
    @Subcommand("setup")
    @CommandCompletion("@configKeys")
    @Syntax("<key> <value>")
    public void onSetupCommand(AbstractUser user, @Optional String key, @Optional String value) {
        CommonUser commonUser = user.user();
        if (commonUser instanceof CommonConsoleUser) {
            commonUser.sendMessage("[MineStore] You can't use this command from console!");
            return;
        }
        if (key == null || value == null) {
            for (ConfigKey configKey : ConfigKey.values()) {
                String keyName = configKey.name().toUpperCase();
                String keyValue = plugin.configReader().get(configKey).toString();
                commonUser.sendMessage(Component.text(keyName).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
                commonUser.sendMessage(Component.text("Current value: ").color(NamedTextColor.GRAY).append(Component.text(keyValue).color(NamedTextColor.WHITE)).append(Component.text(" | ").color(NamedTextColor.GRAY)).append(Component.text("CHANGE VALUE: /minestore setup " + keyName + " <value>").color(NamedTextColor.RED)).decorate(TextDecoration.BOLD));
            }
            return;
        }
        ConfigKey configKey = ConfigKey.valueOf(key.toUpperCase());
        if (configKey == null) {
            commonUser.sendMessage(Component.text("Invalid key!").color(NamedTextColor.RED));
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
    }
}
