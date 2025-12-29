package me.chrommob.minestore.virtualcurrency.providers;

import me.chrommob.minestore.api.interfaces.economyInfo.PlayerEconomyProvider;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.virtualcurrency.VirtualCurrencyAddon;

public class VirtualEconomyProvider implements PlayerEconomyProvider {

    @Override
    public double getBalance(CommonUser commonUser) {
        Double balance = VirtualCurrencyAddon.getDatabaseManager().getBalance(commonUser.getName());
        return balance != null ? balance : 0.0;
    }

    @Override
    public boolean takeMoney(CommonUser commonUser, double amount) {
        if (amount <= 0) {
            return false;
        }
        double currentBalance = getBalance(commonUser);
        if (currentBalance < amount) {
            return false;
        }
        VirtualCurrencyAddon.getDatabaseManager().addBalance(commonUser.getName(), -amount);
        return true;
    }
}
