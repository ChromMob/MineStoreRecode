package me.chrommob.minestore.common.interfaces.user;

import me.chrommob.minestore.common.MineStoreCommon;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public abstract class CommonUser {
    public abstract String getName();
    public abstract void sendMessage(String message);

    public abstract void sendMessage(Component message);

    public abstract boolean hasPermission(String permission);

    public abstract boolean isOnline();

    public abstract UUID getUUID();


    public String getPrefix() {
        return MineStoreCommon.getInstance().playerInfoProvider().getPrefix(this);
    }

    public String getSuffix() {
        return MineStoreCommon.getInstance().playerInfoProvider().getSuffix(this);
    }


    public double getBalance() {
        return MineStoreCommon.getInstance().playerEconomyProvider().getBalance(this);
    }


    public String getGroup() {
        return MineStoreCommon.getInstance().playerInfoProvider().getGroup(this);
    }
}
