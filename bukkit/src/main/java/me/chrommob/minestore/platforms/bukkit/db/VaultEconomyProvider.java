package me.chrommob.minestore.platforms.bukkit.db;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.interfaces.economyInfo.PlayerEconomyProvider;
import me.chrommob.minestore.common.interfaces.user.CommonUser;
import me.chrommob.minestore.platforms.bukkit.MineStoreBukkit;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultEconomyProvider implements PlayerEconomyProvider {
    private final MineStoreBukkit mineStoreBukkit;
    private final Economy economy;
    public VaultEconomyProvider(MineStoreBukkit mineStoreBukkit, MineStoreCommon plugin) {
        this.mineStoreBukkit = mineStoreBukkit;
        Economy economy = null;
        try {
            RegisteredServiceProvider<Economy> rsp = mineStoreBukkit.getServer().getServicesManager().getRegistration(Economy.class);
            economy = rsp.getProvider();
        } catch (Exception e) {
            plugin.log("No economy plugin is installed.");
        }
        this.economy = economy;
    }

    public boolean isInstalled() {
        return economy != null;
    }

    @Override
    public double getBalance(CommonUser commonUser) {
        Player player = mineStoreBukkit.getServer().getPlayer(commonUser.getUUID());
        return economy.getBalance(player);
    }
}
