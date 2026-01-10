package me.chrommob.minestore.virtualcurrency.commands;

import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.virtualcurrency.VirtualCurrencyAddon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class LeaderboardCommand {

    @Command("currency top")
    @Permission("currency.top")
    public void showLeaderboard(AbstractUser sender) {
        showLeaderboard(sender.commonUser());
    }

    public static void showLeaderboard(CommonUser user) {
        int limit = VirtualCurrencyAddon.getTopLimit();
        java.util.List<String[]> topPlayers = VirtualCurrencyAddon.getDatabaseManager().getTopBalances(limit);

        user.sendMessage(Component.text("=== Richest Players ===").color(NamedTextColor.GOLD));

        if (topPlayers.isEmpty()) {
            user.sendMessage(Component.text("No players found.").color(NamedTextColor.GRAY));
            return;
        }

        int rank = 1;
        for (String[] player : topPlayers) {
            String username = player[0];
            double balance = Double.parseDouble(player[1]);

            NamedTextColor rankColor;
            switch (rank) {
                case 1:
                    rankColor = NamedTextColor.GOLD;
                    break;
                case 2:
                    rankColor = NamedTextColor.GRAY;
                    break;
                case 3:
                    rankColor = NamedTextColor.YELLOW;
                    break;
                default:
                    rankColor = NamedTextColor.WHITE;
            }

            user.sendMessage(Component.text("#" + rank + " ").color(rankColor)
                    .append(Component.text(username).color(NamedTextColor.AQUA))
                    .append(Component.text(": ").color(NamedTextColor.GRAY))
                    .append(Component.text(balance).color(NamedTextColor.GREEN))
                    .append(Component.text(" " + VirtualCurrencyAddon.getCurrencySymbol()).color(NamedTextColor.GRAY)));

            rank++;
        }
    }
}
