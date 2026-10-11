package me.chrommob.minestore.platforms.bukkit.db;

import me.chrommob.minestore.api.interfaces.commands.CommonConsoleUser;
import me.chrommob.minestore.api.interfaces.playerInfo.PlayerInfoProvider;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class VaultPlayerInfoProvider implements PlayerInfoProvider {
    private final JavaPlugin mineStoreBukkit;

    public VaultPlayerInfoProvider(JavaPlugin mineStoreBukkit) {
        this.mineStoreBukkit = mineStoreBukkit;
    }

    // Looked up on every use, the chat plugin may enable after MineStore.
    private Chat chat() {
        RegisteredServiceProvider<Chat> rsp = mineStoreBukkit.getServer().getServicesManager().getRegistration(Chat.class);
        return rsp == null ? null : rsp.getProvider();
    }

    // Works for offline players too, getPlayer would pass null to the chat plugin. A null world means global.
    private OfflinePlayer offlinePlayer(CommonUser commonUser) {
        return mineStoreBukkit.getServer().getOfflinePlayer(commonUser.getUUID());
    }

    @Override
    public String getGroup(CommonUser commonUser) {
        Chat chat = chat();
        if (chat == null || commonUser instanceof CommonConsoleUser) {
            return "";
        }
        return chat.getPrimaryGroup((String) null, offlinePlayer(commonUser));
    }

    @Override
    public String getPrefix(CommonUser commonUser) {
        Chat chat = chat();
        if (chat == null || commonUser instanceof CommonConsoleUser) {
            return "";
        }
        return chat.getPlayerPrefix((String) null, offlinePlayer(commonUser));
    }

    @Override
    public String getSuffix(CommonUser commonUser) {
        Chat chat = chat();
        if (chat == null || commonUser instanceof CommonConsoleUser) {
            return "";
        }
        return chat.getPlayerSuffix((String) null, offlinePlayer(commonUser));
    }
}
