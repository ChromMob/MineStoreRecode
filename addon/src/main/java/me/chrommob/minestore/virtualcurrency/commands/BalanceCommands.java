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

public class BalanceCommands {

    @Command("currency give <player> <amount>")
    @Permission("currency.give")
    public void giveBalance(AbstractUser sender, @Argument(value = "player", suggestions = "playerNames") String player, @Argument(value = "amount", suggestions = "commonAmounts") Double amount) {
        if (amount <= 0) {
            sender.commonUser().sendMessage(Component.text("Amount must be positive!").color(NamedTextColor.RED));
            return;
        }

        if (VirtualCurrencyAddon.getDatabaseManager().getBalance(player) == null) {
            VirtualCurrencyAddon.getDatabaseManager().setBalance(player, 0.0);
        }

        VirtualCurrencyAddon.getDatabaseManager().addBalance(player, amount);
        VirtualCurrencyAddon.getDatabaseManager().recordTransaction(player, "ADMIN_ADD", amount, VirtualCurrencyAddon.getDatabaseManager().getBalance(player), "Given by " + sender.commonUser().getName());

        sender.commonUser().sendMessage(Component.text("Added ")
                .color(NamedTextColor.GREEN)
                .append(Component.text(amount).color(NamedTextColor.GOLD))
                .append(Component.text(" " + VirtualCurrencyAddon.getCurrencySymbol() + " to ").color(NamedTextColor.GREEN))
                .append(Component.text(player).color(NamedTextColor.AQUA)));
    }

    @Command("currency take <player> <amount>")
    @Permission("currency.take")
    public void takeBalance(AbstractUser sender, @Argument(value = "player", suggestions = "playerNames") String player, @Argument(value = "amount", suggestions = "commonAmounts") Double amount) {
        if (amount <= 0) {
            sender.commonUser().sendMessage(Component.text("Amount must be positive!").color(NamedTextColor.RED));
            return;
        }

        if (VirtualCurrencyAddon.getDatabaseManager().getBalance(player) == null) {
            sender.commonUser().sendMessage(Component.text("Player '")
                    .color(NamedTextColor.RED)
                    .append(Component.text(player).color(NamedTextColor.AQUA))
                    .append(Component.text("' not found!")));
            return;
        }

        double currentBalance = VirtualCurrencyAddon.getDatabaseManager().getBalance(player);
        if (currentBalance < amount) {
            sender.commonUser().sendMessage(Component.text("Player has insufficient balance!").color(NamedTextColor.RED));
            return;
        }

        VirtualCurrencyAddon.getDatabaseManager().addBalance(player, -amount);
        VirtualCurrencyAddon.getDatabaseManager().recordTransaction(player, "ADMIN_TAKE", -amount, VirtualCurrencyAddon.getDatabaseManager().getBalance(player), "Taken by " + sender.commonUser().getName());

        sender.commonUser().sendMessage(Component.text("Took ")
                .color(NamedTextColor.GREEN)
                .append(Component.text(amount).color(NamedTextColor.GOLD))
                .append(Component.text(" " + VirtualCurrencyAddon.getCurrencySymbol() + " from ").color(NamedTextColor.GREEN))
                .append(Component.text(player).color(NamedTextColor.AQUA)));
    }

    @Command("currency set <player> <amount>")
    @Permission("currency.set")
    public void setBalance(AbstractUser sender, @Argument(value = "player", suggestions = "playerNames") String player, @Argument(value = "amount", suggestions = "commonAmounts") Double amount) {
        if (amount < 0) {
            sender.commonUser().sendMessage(Component.text("Amount cannot be negative!").color(NamedTextColor.RED));
            return;
        }

        double oldBalance = VirtualCurrencyAddon.getDatabaseManager().getBalance(player) != null ? VirtualCurrencyAddon.getDatabaseManager().getBalance(player) : 0.0;

        VirtualCurrencyAddon.getDatabaseManager().setBalance(player, amount);
        VirtualCurrencyAddon.getDatabaseManager().recordTransaction(player, "ADMIN_SET", amount - oldBalance, amount, "Set by " + sender.commonUser().getName());

        sender.commonUser().sendMessage(Component.text("Set balance of ")
                .color(NamedTextColor.GREEN)
                .append(Component.text(player).color(NamedTextColor.AQUA))
                .append(Component.text(" from ").color(NamedTextColor.GRAY))
                .append(Component.text(oldBalance).color(NamedTextColor.GOLD))
                .append(Component.text(" to ").color(NamedTextColor.GREEN))
                .append(Component.text(amount).color(NamedTextColor.GOLD)));
    }

    @Command("currency balance [player]")
    @Permission("currency.balance")
    public void checkBalance(AbstractUser sender, @Argument(value = "player", suggestions = "playerNames") String player) {
        String targetPlayer = player != null ? player : sender.commonUser().getName();

        Double balance = VirtualCurrencyAddon.getDatabaseManager().getBalance(targetPlayer);

        if (balance == null) {
            sender.commonUser().sendMessage(Component.text("Player '")
                    .color(NamedTextColor.RED)
                    .append(Component.text(targetPlayer).color(NamedTextColor.AQUA))
                    .append(Component.text("' not found!")));
        } else {
            sender.commonUser().sendMessage(Component.text(targetPlayer)
                    .color(NamedTextColor.AQUA)
                    .append(Component.text("'s balance: ").color(NamedTextColor.GRAY))
                    .append(Component.text(balance).color(NamedTextColor.GOLD))
                    .append(Component.text(" " + VirtualCurrencyAddon.getCurrencySymbol()).color(NamedTextColor.GREEN)));
        }
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
