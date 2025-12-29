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

public class AdminCommand {

    @Command("currency stats")
    @Permission("currency.stats")
    public void showStats(AbstractUser sender) {
        int totalPlayers = VirtualCurrencyAddon.getDatabaseManager().getTotalPlayers();
        double totalEconomy = VirtualCurrencyAddon.getDatabaseManager().getTotalEconomyBalance();
        long totalTransactions = VirtualCurrencyAddon.getDatabaseManager().getTotalTransactions();

        sender.commonUser().sendMessage(Component.text("=== Economy Statistics ===").color(NamedTextColor.GOLD));
        sender.commonUser().sendMessage(Component.text("Total Players: ").color(NamedTextColor.GRAY)
                .append(Component.text(totalPlayers).color(NamedTextColor.AQUA)));
        sender.commonUser().sendMessage(Component.text("Total Economy: ").color(NamedTextColor.GRAY)
                .append(Component.text(totalEconomy).color(NamedTextColor.GREEN))
                .append(Component.text(" " + VirtualCurrencyAddon.getCurrencySymbol()).color(NamedTextColor.GRAY)));
        sender.commonUser().sendMessage(Component.text("Total Transactions: ").color(NamedTextColor.GRAY)
                .append(Component.text(totalTransactions).color(NamedTextColor.YELLOW)));
        sender.commonUser().sendMessage(Component.text("Average Balance: ").color(NamedTextColor.GRAY)
                .append(Component.text(totalPlayers > 0 ? String.format("%.2f", totalEconomy / totalPlayers) : "0").color(NamedTextColor.GREEN)));
    }

    @Command("currency reset <player>")
    @Permission("currency.reset")
    public void resetPlayer(AbstractUser sender, @Argument(value = "player", suggestions = "playerNames") String player) {
        Double currentBalance = VirtualCurrencyAddon.getDatabaseManager().getBalance(player);

        if (currentBalance == null) {
            sender.commonUser().sendMessage(Component.text("Player '")
                    .color(NamedTextColor.RED)
                    .append(Component.text(player).color(NamedTextColor.AQUA))
                    .append(Component.text("' not found!")));
            return;
        }

        VirtualCurrencyAddon.getDatabaseManager().setBalance(player, 0.0);
        VirtualCurrencyAddon.getDatabaseManager().recordTransaction(player, "ADMIN_SET", -currentBalance, 0.0, "Balance reset by admin");

        sender.commonUser().sendMessage(Component.text("Reset balance of ")
                .color(NamedTextColor.GREEN)
                .append(Component.text(player).color(NamedTextColor.AQUA))
                .append(Component.text(" from ").color(NamedTextColor.GRAY))
                .append(Component.text(currentBalance).color(NamedTextColor.GOLD))
                .append(Component.text(" to 0").color(NamedTextColor.GREEN)));
    }

    @Suggestions("commonAmounts")
    public Set<String> suggestAmounts(CommandContext<AbstractUser> context, CommandInput input) {
        return new HashSet<>(java.util.Arrays.asList("10", "25", "50", "100", "250", "500", "1000"));
    }

    @Suggestions("commonLimits")
    public Set<String> suggestLimits(CommandContext<AbstractUser> context, CommandInput input) {
        return new HashSet<>(java.util.Arrays.asList("5", "10", "25", "50", "100"));
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
