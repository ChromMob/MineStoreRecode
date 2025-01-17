package me.chrommob.minestore.platforms.bukkit.db;

import me.chrommob.minestore.api.interfaces.playerInfo.PlayerInfoProvider;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.platforms.bukkit.MineStoreBukkit;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultPlayerInfoProvider implements PlayerInfoProvider {
    private final MineStoreBukkit mineStoreBukkit;
    private final Chat chat;
    public VaultPlayerInfoProvider(MineStoreBukkit mineStoreBukkit, MineStoreCommon plugin) {
        this.mineStoreBukkit = mineStoreBukkit;
        Chat chat = null;
        try {
            RegisteredServiceProvider<Chat> rsp = mineStoreBukkit.getServer().getServicesManager().getRegistration(Chat.class);
            chat = rsp.getProvider();
        } catch (Exception e) {
            plugin.log("No chat plugin is installed.");
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
