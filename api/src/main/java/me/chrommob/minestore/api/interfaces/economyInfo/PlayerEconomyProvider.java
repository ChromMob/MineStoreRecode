package me.chrommob.minestore.api.interfaces.economyInfo;

import me.chrommob.minestore.api.interfaces.user.CommonUser;

public interface PlayerEconomyProvider {
    double getBalance(CommonUser commonUser);

    boolean takeMoney(CommonUser commonUser, double amount);
}
