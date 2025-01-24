package me.chrommob.minestore.platforms.bukkit.placeholder;

import me.chrommob.minestore.api.interfaces.placeholder.CommonPlaceHolderProvider;
import me.chrommob.minestore.api.placeholder.PlaceHolderManager;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.placeholder.PlaceHolderData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
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
    public String onPlaceholderRequest(Player p, @NotNull String params) {
        PlaceHolderData data = plugin.placeHolderData();
        plugin.debug("Placeholder: " + params);
        if (data == null) {
            return "";
        }
        String result = PlaceHolderManager.getInstance().getResult(p.getName(), params);
        plugin.debug("Placeholder: " + params + " = " + result);
        return result;
    }
}
