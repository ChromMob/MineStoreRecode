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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HistoryCommand {

    @Command("currency history [player] [limit]")
    @Permission("currency.history")
    public void viewHistory(AbstractUser sender, @Argument(value = "player", suggestions = "playerNames") String player, @Argument(value = "limit", suggestions = "commonLimits") Integer limit) {
        String targetPlayer = player != null ? player : sender.commonUser().getName();
        int historyLimit = limit != null ? Math.min(limit, 50) : 10;

        if (VirtualCurrencyAddon.getDatabaseManager().getBalance(targetPlayer) == null) {
            sender.commonUser().sendMessage(Component.text("Player '")
                    .color(NamedTextColor.RED)
                    .append(Component.text(targetPlayer).color(NamedTextColor.AQUA))
                    .append(Component.text("' not found!")));
            return;
        }

        List<String[]> history = VirtualCurrencyAddon.getDatabaseManager().getTransactionHistory(targetPlayer, historyLimit);

        sender.commonUser().sendMessage(Component.text("=== Transaction History: " + targetPlayer + " ===").color(NamedTextColor.GOLD));

        if (history.isEmpty()) {
            sender.commonUser().sendMessage(Component.text("No transactions found.").color(NamedTextColor.GRAY));
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

        for (String[] tx : history) {
            String action = tx[0];
            double amount = Double.parseDouble(tx[1]);
            double balanceAfter = Double.parseDouble(tx[2]);
            String reason = tx[3];
            long timestamp = Long.parseLong(tx[4]);

            NamedTextColor amountColor = amount >= 0 ? NamedTextColor.GREEN : NamedTextColor.RED;
            String sign = amount >= 0 ? "+" : "";

            NamedTextColor actionColor;
            switch (action) {
                case "PAY":
                    actionColor = NamedTextColor.RED;
                    break;
                case "RECEIVE":
                    actionColor = NamedTextColor.GREEN;
                    break;
                case "ADMIN_ADD":
                    actionColor = NamedTextColor.GREEN;
                    break;
                case "ADMIN_SET":
                    actionColor = NamedTextColor.BLUE;
                    break;
                default:
                    actionColor = NamedTextColor.GRAY;
            }
            Component actionComponent = Component.text(action).color(actionColor);

            sender.commonUser().sendMessage(Component.text("[")
                    .color(NamedTextColor.GRAY)
                    .append(Component.text(formatter.format(Instant.ofEpochMilli(timestamp))))
                    .append(Component.text("] ").color(NamedTextColor.GRAY))
                    .append(actionComponent)
                    .append(Component.text(" " + sign + amount + " " + VirtualCurrencyAddon.getCurrencySymbol()).color(amountColor))
                    .append(Component.text(" (Balance: " + balanceAfter + ")").color(NamedTextColor.GRAY))
                    .append(Component.text(" - " + reason).color(NamedTextColor.DARK_GRAY)));
        }
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
