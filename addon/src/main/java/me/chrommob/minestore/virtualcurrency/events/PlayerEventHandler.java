package me.chrommob.minestore.virtualcurrency.events;

import me.chrommob.minestore.api.event.types.MineStorePlayerJoinEvent;
import me.chrommob.minestore.virtualcurrency.VirtualCurrencyAddon;

public class PlayerEventHandler {

    public void handle(MineStorePlayerJoinEvent event) {
        String username = event.getUsername();

        if (VirtualCurrencyAddon.getDatabaseManager().getBalance(username) == null) {
            VirtualCurrencyAddon.getDatabaseManager().setBalance(username, VirtualCurrencyAddon.getDefaultBalance());
        }

        VirtualCurrencyAddon.getDatabaseManager().recordTransaction(username, "JOIN", 0, VirtualCurrencyAddon.getDatabaseManager().getBalance(username), "Player joined");

        if (VirtualCurrencyAddon.isDailyBonusEnabled()) {
            handleDailyBonus(username);
        }
    }

    private void handleDailyBonus(String username) {
        long lastClaim = VirtualCurrencyAddon.getDatabaseManager().getLastClaimDate(username);
        long now = System.currentTimeMillis();
        long oneDayMs = 24 * 60 * 60 * 1000L;

        boolean canClaim = lastClaim == 0 || (now - lastClaim) >= oneDayMs;

        if (canClaim) {
            int streak = VirtualCurrencyAddon.getDatabaseManager().getLoginStreak(username);

            if (lastClaim > 0 && (now - lastClaim) < 2 * oneDayMs) {
                streak++;
            } else {
                streak = 1;
            }

            double bonusAmount = VirtualCurrencyAddon.getDailyBonusAmount() * Math.min(streak, 7);

            VirtualCurrencyAddon.getDatabaseManager().addBalance(username, bonusAmount);
            VirtualCurrencyAddon.getDatabaseManager().updateDailyLogin(username, now, streak);
            VirtualCurrencyAddon.getDatabaseManager().recordTransaction(username, "DAILY_BONUS", bonusAmount, VirtualCurrencyAddon.getDatabaseManager().getBalance(username), "Daily bonus (streak: " + streak + ")");
        }
    }
}
