package me.chrommob.minestore.virtualcurrency.gui;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.gui.CommonItem;
import me.chrommob.minestore.api.interfaces.gui.EnchantmentData;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.virtualcurrency.VirtualCurrencyAddon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardGui {

    private static final int ITEMS_PER_PAGE = 21;
    private static final int PAGE_SIZE = 27;

    public static void openLeaderboard(CommonUser user) {
        Registries.SCHEDULER.get().run(() -> {
            user.openInventory(createLeaderboardGui(1));
        });
    }

    public static CommonInventory createLeaderboardGui(int page) {
        Component title = Component.text("Richest Players")
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.GOLD)
                .append(Component.text(" (" + page + ")").color(NamedTextColor.GRAY));

        CommonItem[] items = new CommonItem[PAGE_SIZE];

        int totalPlayers = VirtualCurrencyAddon.getDatabaseManager().getTotalPlayers();
        int totalPages = (int) Math.ceil((double) totalPlayers / ITEMS_PER_PAGE);
        if (totalPages < 1) totalPages = 1;

        int offset = (page - 1) * ITEMS_PER_PAGE;
        java.util.List<String[]> topPlayers = VirtualCurrencyAddon.getDatabaseManager().getTopBalances(ITEMS_PER_PAGE + offset);
        List<String[]> pagePlayers = topPlayers.subList(offset, Math.min(offset + ITEMS_PER_PAGE, topPlayers.size()));

        int slot = 0;
        for (String[] player : pagePlayers) {
            if (slot >= ITEMS_PER_PAGE) break;

            String username = player[0];
            double balance = Double.parseDouble(player[1]);

            CommonItem playerItem = createPlayerItem(username, balance, offset + slot + 1);
            items[slot] = playerItem;
            slot++;
        }

        for (int i = items.length - 1; i >= 0; i--) {
            if (items[i] == null) {
                items[i] = createFillerItem();
            }
        }

        int buttonSlot = ITEMS_PER_PAGE;

        if (page > 1) {
            CommonItem prevItem = createPrevItem(page);
            items[buttonSlot] = prevItem;
        }

        CommonItem backItem = createBackItem();
        items[buttonSlot + 1] = backItem;

        if (page < totalPages) {
            CommonItem nextItem = createNextItem(page);
            items[buttonSlot + 2] = nextItem;
        }

        CommonInventory inventory = new CommonInventory(title, PAGE_SIZE);
        for (int i = 0; i < items.length; i++) {
            inventory.setItem(i, items[i]);
        }

        return inventory;
    }

    private static CommonItem createPlayerItem(String username, double balance, int rank) {
        NamedTextColor rankColor;
        String rankPrefix;
        switch (rank) {
            case 1:
                rankColor = NamedTextColor.GOLD;
                rankPrefix = "1st";
                break;
            case 2:
                rankColor = NamedTextColor.GRAY;
                rankPrefix = "2nd";
                break;
            case 3:
                rankColor = NamedTextColor.YELLOW;
                rankPrefix = "3rd";
                break;
            default:
                rankColor = NamedTextColor.WHITE;
                rankPrefix = rank + "th";
        }

        Component name = Component.text(rankPrefix + " - " + username)
                .decorate(TextDecoration.BOLD)
                .color(rankColor);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Balance: ").color(NamedTextColor.GRAY)
                .append(Component.text(formatBalance(balance)).color(NamedTextColor.GREEN))
                .append(Component.text(" " + VirtualCurrencyAddon.getCurrencySymbol()).color(NamedTextColor.GRAY)));

        String material;
        List<EnchantmentData> enchantments = null;
        switch (rank) {
            case 1:
                material = "GOLD_BLOCK";
                enchantments = Collections.singletonList(new EnchantmentData("unbreaking", 1));
                break;
            case 2:
                material = "IRON_BLOCK";
                enchantments = Collections.singletonList(new EnchantmentData("unbreaking", 1));
                break;
            case 3:
                material = "COPPER_BLOCK";
                enchantments = Collections.singletonList(new EnchantmentData("unbreaking", 1));
                break;
            default:
                material = "PLAYER_HEAD";
                break;
        }

        return new CommonItem(name, material, lore, enchantments, 1, null);
    }

    private static CommonItem createPrevItem(int page) {
        Component name = Component.text("Previous")
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.GREEN);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Page " + (page - 1)).color(NamedTextColor.GRAY));

        return new CommonItem(name, "ARROW", lore, event -> {
            event.getUser().closeInventory();
            Registries.SCHEDULER.get().run(() -> {
                event.getUser().openInventory(createLeaderboardGui(page - 1));
            });
        });
    }

    private static CommonItem createNextItem(int page) {
        Component name = Component.text("Next")
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.GREEN);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Page " + (page + 1)).color(NamedTextColor.GRAY));

        return new CommonItem(name, "ARROW", lore, event -> {
            event.getUser().closeInventory();
            Registries.SCHEDULER.get().run(() -> {
                event.getUser().openInventory(createLeaderboardGui(page + 1));
            });
        });
    }

    private static CommonItem createBackItem() {
        Component name = Component.text("Back")
                .decorate(TextDecoration.BOLD)
                .color(NamedTextColor.RED);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Go to balance").color(NamedTextColor.GRAY));

        return new CommonItem(name, "BARRIER", lore, event -> {
            event.getUser().closeInventory();
            Registries.SCHEDULER.get().run(() -> {
                String playerName = event.getUser().getName();
                event.getUser().openInventory(CurrencyGui.createBalanceGui(playerName));
            });
        });
    }

    private static CommonItem createFillerItem() {
        Component name = Component.empty();
        List<Component> lore = new ArrayList<>();
        return new CommonItem(name, "GRAY_STAINED_GLASS_PANE", lore);
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
