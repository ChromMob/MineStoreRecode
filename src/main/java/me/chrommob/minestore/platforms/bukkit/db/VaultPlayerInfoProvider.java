package me.chrommob.minestore.platforms.bukkit.db;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.interfaces.playerInfo.PlayerInfoProvider;
import me.chrommob.minestore.common.interfaces.user.CommonUser;
import me.chrommob.minestore.platforms.bukkit.MineStoreBukkit;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultPlayerInfoProvider implements PlayerInfoProvider {
    private final MineStoreBukkit mineStoreBukkit;
    private final Chat chat;
    public VaultPlayerInfoProvider(MineStoreBukkit mineStoreBukkit) {
        this.mineStoreBukkit = mineStoreBukkit;
        Chat chat = null;
        try {
            RegisteredServiceProvider<Chat> rsp = mineStoreBukkit.getServer().getServicesManager().getRegistration(Chat.class);
            chat = rsp.getProvider();
        } catch (IllegalStateException e) {
            MineStoreCommon.getInstance().debug("Vault is not installed on this server.");
        }
        this.chat = chat;
    }

    public boolean isInstalled() {
        return chat != null;
    }

    @Override
    public String getGroup(CommonUser commonUser) {
        Player player = Bukkit.getPlayer(commonUser.getUUID());
        return chat.getPrimaryGroup(player);
    }

    @Override
    public String getPrefix(CommonUser commonUser) {
        Player player = Bukkit.getPlayer(commonUser.getUUID());
        return chat.getPlayerPrefix(player);
    }

    @Override
    public String getSuffix(CommonUser commonUser) {
        return chat.getPlayerSuffix(Bukkit.getPlayer(commonUser.getUUID()));
    }
}
