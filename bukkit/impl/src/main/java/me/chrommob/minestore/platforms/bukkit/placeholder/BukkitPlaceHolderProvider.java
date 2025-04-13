package me.chrommob.minestore.platforms.bukkit.placeholder;

import me.chrommob.minestore.api.interfaces.placeholder.CommonPlaceHolderProvider;
import me.chrommob.minestore.api.placeholder.PlaceHolderManager;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.placeholder.PlaceHolderData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class BukkitPlaceHolderProvider extends PlaceholderExpansion implements CommonPlaceHolderProvider {
    private final MineStoreCommon plugin;
    public BukkitPlaceHolderProvider(MineStoreCommon pl) {
        this.plugin = pl;
    }

    @Override
    public boolean init() {
        return this.register();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ms";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return "ChromMob";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer p, @NotNull String params) {
        PlaceHolderData data = plugin.placeHolderData();
        if (data == null) {
            return "";
        }
        String name;
        if (p == null) {
            name = "";
        } else {
            name = p.getName();
        }
        String result = PlaceHolderManager.getInstance().getResult(name, params);
        plugin.debug(this.getClass(), "Placeholder: " + params + " = " + result);
        return result;
    }
}
