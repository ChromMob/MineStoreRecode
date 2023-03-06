package me.chrommob.minestore.platforms.bukkit.placeholder;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.interfaces.placeholder.CommonPlaceHolderProvider;
import me.chrommob.minestore.common.placeholder.PlaceHolderData;
import me.chrommob.minestore.platforms.bukkit.MineStoreBukkit;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BukkitPlaceHolderProvider extends PlaceholderExpansion implements CommonPlaceHolderProvider {
    private final MineStoreBukkit plugin;
    public BukkitPlaceHolderProvider(MineStoreBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        if (!this.register()) {
            MineStoreCommon.getInstance().log("Failed to register PlaceHolderAPI expansion!");
        }
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
        PlaceHolderData data = MineStoreCommon.getInstance().placeHolderData();
        MineStoreCommon.getInstance().debug("Placeholder: " + params);
        if (data == null) {
            return "";
        }
        if (params.contains("top_donator_username_")) {
            int arg = Integer.parseInt(params.replaceFirst("top_donator_username_", ""));
            try {
                return data.getTopDonators().get(arg + 1).getUserName();
            } catch (Exception ignored) {
            }
        }
        if (params.contains("top_donator_price_")) {
            int arg = Integer.parseInt(params.replaceFirst("top_donator_price_", ""));
            try {
                return String.valueOf(data.getTopDonators().get(arg + 1).getPrice());
            } catch (Exception ignored) {
            }
        }
        if (params.contains("last_donator_username_")) {
            int arg = Integer.parseInt(params.replaceFirst("last_donator_username_", ""));
            try {
                return data.getLastDonators().get(arg + 1).getUserName();
            } catch (Exception ignored) {
            }
        }
        if (params.contains("last_donator_price_")) {
            int arg = Integer.parseInt(params.replaceFirst("last_donator_price_", ""));
            try {
                return String.valueOf(data.getLastDonators().get(arg + 1).getPrice());
            } catch (Exception ignored) {
            }
        }
        if (params.contains("last_donator_package_")) {
            int arg = Integer.parseInt(params.replaceFirst("last_donator_package_", ""));
            try {
                return data.getLastDonators().get(arg + 1).getPackageName();
            } catch (Exception ignored) {
            }
        }
        if (params.contains("donation_goal_current")) {
            return String.valueOf(data.getDonationGoal().getDonationGoalCurrentAmount());
        }
        if (params.contains("donation_goal_target")) {
            return String.valueOf(data.getDonationGoal().getDonationGoalAmount());
        }
        if (params.contains("donation_goal_percentage")) {
            return String.valueOf(data.getDonationGoal().getDonationGoalPercentage());
        }
        return "";
    }
}
