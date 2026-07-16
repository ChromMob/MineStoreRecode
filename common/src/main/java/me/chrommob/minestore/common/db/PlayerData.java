package me.chrommob.minestore.common.db;

import me.chrommob.minestore.api.interfaces.user.CommonUser;

import java.util.Objects;
import java.util.UUID;

public class PlayerData {
    private final CommonUser user;
    private final UUID uuid;
    private final String name;
    private String prefix;
    private String suffix;
    private double balance;
    private String playerGroup;
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
        return hasChanged(true, true, true, true);
    }

    public boolean hasChanged(boolean syncBalance, boolean syncPrefix, boolean syncSuffix, boolean syncPlayerGroup) {
        if (firstJoin) {
            return true;
        }
        return (syncPrefix && !Objects.equals(user.getPrefix(), prefix))
                || (syncSuffix && !Objects.equals(user.getSuffix(), suffix))
                || (syncBalance && Double.compare(user.getBalance(), balance) != 0)
                || (syncPlayerGroup && !Objects.equals(user.getGroup(), playerGroup));
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return user.getPrefix();
    }

    public String getSuffix() {
        return user.getSuffix();
    }

    public double getBalance() {
        return user.getBalance();
    }

    public String getPlayerGroup() {
        return user.getGroup();
    }

    public void markSynced(boolean syncBalance, boolean syncPrefix, boolean syncSuffix, boolean syncPlayerGroup,
                           double balance, String prefix, String suffix, String playerGroup) {
        firstJoin = false;
        if (syncBalance) this.balance = balance;
        if (syncPrefix) this.prefix = prefix;
        if (syncSuffix) this.suffix = suffix;
        if (syncPlayerGroup) this.playerGroup = playerGroup;
    }
}
