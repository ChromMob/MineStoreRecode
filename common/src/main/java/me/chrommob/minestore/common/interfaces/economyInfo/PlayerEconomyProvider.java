package me.chrommob.minestore.common.interfaces.economyInfo;

import me.chrommob.minestore.common.interfaces.user.CommonUser;

public interface PlayerEconomyProvider {
    double getBalance(CommonUser commonUser);
}
