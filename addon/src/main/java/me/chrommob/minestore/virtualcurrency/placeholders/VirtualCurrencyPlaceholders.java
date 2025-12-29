package me.chrommob.minestore.virtualcurrency.placeholders;

import me.chrommob.minestore.virtualcurrency.VirtualCurrencyAddon;

import java.util.function.BiFunction;

public class VirtualCurrencyPlaceholders {

    public static String getBalance(String player, String placeholder) {
        Double balance = VirtualCurrencyAddon.getDatabaseManager().getBalance(player);
        if (balance == null) {
            return "0";
        }
        return String.valueOf(balance);
    }

    public static String getCurrencyName(String player, String placeholder) {
        return VirtualCurrencyAddon.getCurrencyName();
    }

    public static String getRank(String player, String placeholder) {
        int rank = VirtualCurrencyAddon.getDatabaseManager().getRank(player);
        return rank > 0 ? String.valueOf(rank) : "-";
    }

    public static String isPayEnabled(String player, String placeholder) {
        return VirtualCurrencyAddon.isPayEnabled() ? "true" : "false";
    }
}
