package me.chrommob.minestore.virtualcurrency.commands;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.virtualcurrency.VirtualCurrencyAddon;
import me.chrommob.minestore.virtualcurrency.gui.CurrencyGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class GuiCommand {

    @Command("currency gui")
    @Permission("currency.gui")
    public void openGui(AbstractUser sender) {
        String playerName = sender.commonUser().getName();
        Registries.SCHEDULER.get().run(() -> {
            sender.commonUser().openInventory(CurrencyGui.createBalanceGui(playerName));
        });
    }

    @Command("currency gui <player>")
    @Permission("currency.gui.others")
    public void openGuiOther(AbstractUser sender, @Argument(value = "player") String playerName) {
        Double balance = VirtualCurrencyAddon.getDatabaseManager().getBalance(playerName);
        if (balance == null) {
            sender.commonUser().sendMessage(Component.text("Player '")
                    .color(NamedTextColor.RED)
                    .append(Component.text(playerName).color(NamedTextColor.AQUA))
                    .append(Component.text("' not found!")));
            return;
        }
        Registries.SCHEDULER.get().run(() -> {
            sender.commonUser().openInventory(CurrencyGui.createBalanceGui(playerName));
        });
    }
}
