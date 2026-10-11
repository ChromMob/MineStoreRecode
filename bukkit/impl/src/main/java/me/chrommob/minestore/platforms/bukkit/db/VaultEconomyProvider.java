package me.chrommob.minestore.platforms.bukkit.db;

import me.chrommob.minestore.api.interfaces.commands.CommonConsoleUser;
import me.chrommob.minestore.api.interfaces.economyInfo.PlayerEconomyProvider;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class VaultEconomyProvider implements PlayerEconomyProvider {
    private final JavaPlugin mineStoreBukkit;

    public VaultEconomyProvider(JavaPlugin mineStoreBukkit) {
        this.mineStoreBukkit = mineStoreBukkit;
    }

    // Looked up on every use, the economy plugin may enable after MineStore.
    private Economy economy() {
        RegisteredServiceProvider<Economy> rsp = mineStoreBukkit.getServer().getServicesManager().getRegistration(Economy.class);
        return rsp == null ? null : rsp.getProvider();
    }

    // Works for offline players too, getPlayer would pass null to the economy plugin.
    private OfflinePlayer offlinePlayer(CommonUser commonUser) {
        return mineStoreBukkit.getServer().getOfflinePlayer(commonUser.getUUID());
    }

    @Override
    public double getBalance(CommonUser commonUser) {
        Economy economy = economy();
        if (economy == null || commonUser instanceof CommonConsoleUser) {
            return 0;
        }
        return economy.getBalance(offlinePlayer(commonUser));
    }

    @Override
    public boolean takeMoney(CommonUser commonUser, double amount) {
        Economy economy = economy();
        if (economy == null || commonUser instanceof CommonConsoleUser) {
            return false;
        }
        return economy.withdrawPlayer(offlinePlayer(commonUser), amount).transactionSuccess();
    }
}
