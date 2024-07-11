package me.chrommob.minestore.common.interfaces.user;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.interfaces.gui.CommonInventory;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public abstract class CommonUser {
    private final MineStoreCommon plugin;
    public CommonUser(MineStoreCommon plugin) {
        this.plugin = plugin;
    }
    public abstract String getName();
    public abstract void sendMessage(String message);

    public abstract void sendMessage(Component message);

    public abstract boolean hasPermission(String permission);

    public abstract boolean isOnline();

    public abstract UUID getUUID();

    public abstract void openInventory(CommonInventory inventory);
    public abstract void closeInventory();

    public String getPrefix() {
        return plugin.playerInfoProvider().getPrefix(this);
    }

    public String getSuffix() {
        return plugin.playerInfoProvider().getSuffix(this);
    }


    public double getBalance() {
        return plugin.playerEconomyProvider().getBalance(this);
    }


    public String getGroup() {
        return plugin.playerInfoProvider().getGroup(this);
    }
}
