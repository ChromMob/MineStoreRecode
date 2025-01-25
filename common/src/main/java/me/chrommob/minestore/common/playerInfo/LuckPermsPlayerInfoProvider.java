package me.chrommob.minestore.common.playerInfo;

import me.chrommob.minestore.api.interfaces.playerInfo.PlayerInfoProvider;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.common.MineStoreCommon;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

public class LuckPermsPlayerInfoProvider implements PlayerInfoProvider {
    private final LuckPerms luckPerms;
    private final MineStoreCommon plugin;
    public LuckPermsPlayerInfoProvider(MineStoreCommon plugin) {
        this.plugin = plugin;
        try {
            Class.forName("net.luckperms.api.LuckPermsProvider");
        } catch (ClassNotFoundException e) {
            plugin.debug(this.getClass(), "LuckPerms are not installed on this server.");
            luckPerms = null;
            return;
        }

        LuckPerms luckPerms = null;
        try {
            luckPerms = LuckPermsProvider.get();
        } catch (IllegalStateException e) {
            plugin.debug(this.getClass(), "LuckPerms are not installed on this server.");
        }
        this.luckPerms = luckPerms;
    }

    public boolean isInstalled() {
        return luckPerms != null;
    }

    @Override
    public String getGroup(CommonUser commonUser) {
        return luckPerms.getUserManager().getUser(commonUser.getUUID()).getPrimaryGroup();
    }

    @Override
    public String getPrefix(CommonUser commonUser) {
        String prefix = luckPerms.getUserManager().getUser(commonUser.getUUID()).getCachedData().getMetaData().getPrefix();
        if (prefix == null) {
            prefix = "";
        }
        return prefix;
    }

    @Override
    public String getSuffix(CommonUser commonUser) {
        String suffix = luckPerms.getUserManager().getUser(commonUser.getUUID()).getCachedData().getMetaData().getSuffix();
        if (suffix == null) {
            suffix = "";
        }
        return suffix;
    }
}
