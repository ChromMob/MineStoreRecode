package me.chrommob.minestore.api.interfaces.user;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;

public abstract class CommonUser {
    public abstract String getName();
    public abstract void sendMessage(String message);

    public abstract void sendTitle(Title title);

    public abstract void sendMessage(Component message);

    public abstract boolean hasPermission(String permission);

    public abstract boolean isOnline();

    public abstract UUID getUUID();

    public abstract InetSocketAddress getAddress();

    public abstract void openInventory(CommonInventory inventory);
    public abstract void closeInventory();

    public String getPrefix() {
        return Registries.PLAYER_INFO_PROVIDER.get().getPrefix(this);
    }

    public String getSuffix() {
        return Registries.PLAYER_INFO_PROVIDER.get().getSuffix(this);
    }

    public double getBalance() {
        return Registries.PLAYER_ECONOMY_PROVIDER.get().getBalance(this);
    }

    public boolean takeMoney(double amount) {
        return Registries.PLAYER_ECONOMY_PROVIDER.get().takeMoney(this, amount);
    }

    public String getGroup() {
        return Registries.PLAYER_INFO_PROVIDER.get().getGroup(this);
    }
}
