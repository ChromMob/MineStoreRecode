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
import org.checkerframework.checker.units.qual.C;

@CommandAlias("minestore|ms")
@CommandPermission("minestore.setup|ms.setup")
public class SetupCommand extends BaseCommand {
    private final MineStoreCommon plugin;
    public SetupCommand(MineStoreCommon plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("unused")
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
                String keyValue = (String) plugin.configReader().get(configKey);
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
            plugin.configReader().set(configKey, type.cast(value));
        } catch (ClassCastException e) {
            commonUser.sendMessage(Component.text("Invalid value!").color(NamedTextColor.RED));
            return;
        }
        commonUser.sendMessage(Component.text("Successfully set ").color(NamedTextColor.GREEN).append(Component.text(key.toUpperCase()).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD)).append(Component.text(" to ").color(NamedTextColor.GREEN)).append(Component.text(value).color(NamedTextColor.WHITE).decorate(TextDecoration.BOLD)));
    }
}
