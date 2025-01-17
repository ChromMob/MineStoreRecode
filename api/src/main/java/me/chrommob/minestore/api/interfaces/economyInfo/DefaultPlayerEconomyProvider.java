package me.chrommob.minestore.api.interfaces.economyInfo;

import me.chrommob.minestore.api.interfaces.user.CommonUser;

public class DefaultPlayerEconomyProvider implements PlayerEconomyProvider {
    @Override
    public double getBalance(CommonUser commonUser) {
        return 0;
    }

    @Override
    public boolean takeMoney(CommonUser commonUser, double amount) {
        return false;
    }
}
