package me.chrommob.minestore.virtualcurrency.gui;

import me.chrommob.minestore.api.event.types.GuiClickEvent;
import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.gui.CommonItem;
import me.chrommob.minestore.virtualcurrency.VirtualCurrencyAddon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CurrencyGui {

    public static CommonInventory createBalanceGui(String playerName) {
        Double balance = VirtualCurrencyAddon.getDatabaseManager().getBalance(playerName);
        int rank = VirtualCurrencyAddon.getDatabaseManager().getRank(playerName);
        int totalPlayers = VirtualCurrencyAddon.getDatabaseManager().getTotalPlayers();

        if (balance == null) {
            balance = 0.0;
        }

        Component title = Component.text(VirtualCurrencyAddon.getCurrencyName())
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.GOLD);

        List<CommonItem> items = new ArrayList<>();

        CommonItem balanceItem = createBalanceItem(balance, playerName);
        items.add(balanceItem);

        CommonItem rankItem = createRankItem(rank, totalPlayers);
        items.add(rankItem);

        CommonItem leaderboardItem = createLeaderboardItem();
        items.add(leaderboardItem);

        CommonItem closeItem = createCloseItem();
        items.add(closeItem);

        for (int i = items.size(); i < 27; i++) {
            CommonItem filler = createFillerItem();
            items.add(filler);
        }

        return new CommonInventory(title, 27, items);
    }

    private static CommonItem createBalanceItem(double balance, String playerName) {
        Component name = Component.text("Your Balance")
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.GREEN);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Player: ").color(NamedTextColor.GRAY)
                .append(Component.text(playerName).color(NamedTextColor.AQUA)));
        lore.add(Component.text("Balance: ").color(NamedTextColor.GRAY)
                .append(Component.text(formatBalance(balance)).color(NamedTextColor.GOLD)));
        lore.add(Component.text(VirtualCurrencyAddon.getCurrencySymbol()).color(NamedTextColor.GREEN));

        return new CommonItem(name, "GOLD_INGOT", lore, event -> {
        });
    }

    private static CommonItem createRankItem(int rank, int totalPlayers) {
        Component name = Component.text("Your Rank")
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.BLUE);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Rank: #").color(NamedTextColor.GRAY)
                .append(Component.text(rank).color(NamedTextColor.GOLD)));
        lore.add(Component.text("Out of ").color(NamedTextColor.GRAY)
                .append(Component.text(totalPlayers).color(NamedTextColor.AQUA))
                .append(Component.text(" players").color(NamedTextColor.GRAY)));

        return new CommonItem(name, "EMERALD", lore, event -> {
        });
    }

    private static CommonItem createLeaderboardItem() {
        Component name = Component.text("Top Players")
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.YELLOW);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Click to view").color(NamedTextColor.GRAY));
        lore.add(Component.text("the leaderboard!").color(NamedTextColor.GRAY));

        return new CommonItem(name, "BOOK", lore, event -> {
            event.getUser().closeInventory();
            me.chrommob.minestore.virtualcurrency.gui.LeaderboardGui.openLeaderboard(event.getUser());
        });
    }

    private static CommonItem createCloseItem() {
        Component name = Component.text("Close")
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.RED);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Click to close").color(NamedTextColor.GRAY));

        return new CommonItem(name, "BARRIER", lore, event -> {
            event.getUser().closeInventory();
        });
    }

    private static CommonItem createFillerItem() {
        Component name = Component.empty();

        List<Component> lore = new ArrayList<>();

        return new CommonItem(name, "GRAY_STAINED_GLASS_PANE", lore, true);
    }

    private static String formatBalance(double balance) {
        int precision = VirtualCurrencyAddon.getBalancePrecision();
        if (precision == 0) {
            return String.format("%.0f", balance);
        } else if (precision == 1) {
            return String.format("%.1f", balance);
        } else {
            return String.format("%.2f", balance);
        }
    }
}
