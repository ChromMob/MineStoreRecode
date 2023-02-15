package me.chrommob.minestore.common.interfaces.economyInfo;

import me.chrommob.minestore.common.interfaces.user.CommonUser;

public class DefaultPlayerEconomyProvider implements PlayerEconomyProvider {
    @Override
    public double getBalance(CommonUser commonUser) {
        return 0;
    }
}
