package me.chrommob.minestore.common.db;

import me.chrommob.minestore.api.interfaces.user.CommonUser;

import java.util.UUID;

public class PlayerData {
    private final CommonUser user;
    private final UUID uuid;
    private final String name;
    private final String prefix;
    private final String suffix;
    private final double balance;
    private final String playerGroup;
    private boolean firstJoin;

    public PlayerData(CommonUser user) {
        this.firstJoin = true;
        this.user = user;
        this.uuid = user.getUUID();
        this.name = user.getName();
        this.prefix = user.getPrefix();
        this.suffix = user.getSuffix();
        this.balance = user.getBalance();
        this.playerGroup = user.getGroup();
    }

    public boolean hasChanged() {
        if (firstJoin) {
            firstJoin = false;
            return true;
        }
        return !user.getPrefix().equals(prefix) || !user.getSuffix().equals(suffix) || user.getBalance() != balance || !user.getGroup().equals(playerGroup);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public double getBalance() {
        return balance;
    }

    public String getPlayerGroup() {
        return playerGroup;
    }
}
