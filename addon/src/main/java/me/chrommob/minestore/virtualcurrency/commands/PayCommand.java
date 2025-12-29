package me.chrommob.minestore.virtualcurrency.commands;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.virtualcurrency.VirtualCurrencyAddon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.suggestion.Suggestions;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;

import java.util.HashSet;
import java.util.Set;

public class PayCommand {

    @Command("currency pay <player> <amount>")
    @Permission("currency.pay")
    public void payPlayer(AbstractUser sender, @Argument(value = "player", suggestions = "playerNames") String target, @Argument(value = "amount", suggestions = "commonAmounts") Double amount) {
        String senderName = sender.commonUser().getName();

        if (target.equalsIgnoreCase(senderName)) {
            sender.commonUser().sendMessage(Component.text("You cannot pay yourself!").color(NamedTextColor.RED));
            return;
        }

        if (amount <= 0) {
            sender.commonUser().sendMessage(Component.text("Amount must be positive!").color(NamedTextColor.RED));
            return;
        }

        Double senderBalance = VirtualCurrencyAddon.getDatabaseManager().getBalance(senderName);
        if (senderBalance == null || senderBalance < amount) {
            sender.commonUser().sendMessage(Component.text("Insufficient balance!").color(NamedTextColor.RED));
            return;
        }

        if (VirtualCurrencyAddon.getDatabaseManager().getBalance(target) == null) {
            VirtualCurrencyAddon.getDatabaseManager().setBalance(target, 0.0);
        }

        VirtualCurrencyAddon.getDatabaseManager().addBalance(senderName, -amount);
        VirtualCurrencyAddon.getDatabaseManager().addBalance(target, amount);

        double newBalance = VirtualCurrencyAddon.getDatabaseManager().getBalance(senderName);

        VirtualCurrencyAddon.getDatabaseManager().recordTransaction(senderName, "PAY", -amount, newBalance, "Paid to " + target);
        VirtualCurrencyAddon.getDatabaseManager().recordTransaction(target, "RECEIVE", amount, VirtualCurrencyAddon.getDatabaseManager().getBalance(target), "Received from " + senderName);

        sender.commonUser().sendMessage(Component.text("You sent ")
                .color(NamedTextColor.GREEN)
                .append(Component.text(amount).color(NamedTextColor.GOLD))
                .append(Component.text(" " + VirtualCurrencyAddon.getCurrencySymbol() + " to ").color(NamedTextColor.GREEN))
                .append(Component.text(target).color(NamedTextColor.AQUA))
                .append(Component.text(". New balance: ").color(NamedTextColor.GRAY))
                .append(Component.text(newBalance).color(NamedTextColor.GOLD)));
    }

    @Suggestions("commonAmounts")
    public Set<String> suggestAmounts(CommandContext<AbstractUser> context, CommandInput input) {
        return new HashSet<>(java.util.Arrays.asList("10", "25", "50", "100", "250", "500", "1000"));
    }

    @Suggestions("playerNames")
    public Set<String> suggestPlayerNames(CommandContext<AbstractUser> context, CommandInput input) {
        Set<String> names = new HashSet<>();
        if (Registries.USER_GETTER.get() != null) {
            Registries.USER_GETTER.get().getAllPlayers().forEach(user -> names.add(user.commonUser().getName()));
        }
        return names;
    }
}
