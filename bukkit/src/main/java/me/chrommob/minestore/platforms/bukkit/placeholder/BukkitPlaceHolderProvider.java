package me.chrommob.minestore.platforms.bukkit.placeholder;

import me.chrommob.minestore.addons.api.WebApiAccessor;
import me.chrommob.minestore.addons.api.placeholder.PlaceHolderManager;
import me.chrommob.minestore.addons.api.profile.ProfileManager;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.interfaces.placeholder.CommonPlaceHolderProvider;
import me.chrommob.minestore.common.placeholder.PlaceHolderData;
import me.chrommob.minestore.platforms.bukkit.MineStoreBukkit;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.BiFunction;
public class BukkitPlaceHolderProvider extends PlaceholderExpansion implements CommonPlaceHolderProvider {
    private final MineStoreCommon plugin;
    public BukkitPlaceHolderProvider(MineStoreBukkit plugin, MineStoreCommon pl) {
        this.plugin = pl;
    }

    @Override
    public void init() {
        if (!this.register()) {
            plugin.log("Failed to register PlaceHolderAPI expansion!");
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
        PlaceHolderData data = plugin.placeHolderData();
        plugin.debug("Placeholder: " + params);
        if (data == null) {
            return "";
        }
        try {
            Map<String, BiFunction<String, String, String>> placeHolders = PlaceHolderManager.getInstance().getPlaceHolders();
            if (placeHolders.containsKey(params)) {
                String value = placeHolders.get(params).apply(p.getName(), params);
                plugin.debug("Placeholder: " + params + " = " + value);
                return value;
            } else {
                String regexTest = placeHolders.keySet().stream()
                        .filter(stringStringFunction -> stringStringFunction.matches(params))
                        .findFirst()
                        .orElse(null);
                if (regexTest != null) {
                    String value = placeHolders.get(regexTest).apply(p.getName(), params);
                    plugin.debug("Placeholder: " + params + " = " + value);
                    return value;
                }
            }
            if (params.contains("top_donator_username_")) {
                int arg = Integer.parseInt(params.replaceFirst("top_donator_username_", ""));
                plugin.debug("Top donator username: " + data.getTopDonators().get(arg - 1).getUserName() + " (" + arg + ")");
                return data.getTopDonators().get(arg - 1).getUserName();
            }
            if (params.contains("top_donator_price_")) {
                int arg = Integer.parseInt(params.replaceFirst("top_donator_price_", ""));
                return String.valueOf(data.getTopDonators().get(arg - 1).getPrice());
            }
            if (params.contains("last_donator_username_")) {
                int arg = Integer.parseInt(params.replaceFirst("last_donator_username_", ""));
                return data.getLastDonators().get(arg - 1).getUserName();
            }
            if (params.contains("last_donator_price_")) {
                int arg = Integer.parseInt(params.replaceFirst("last_donator_price_", ""));
                return String.valueOf(data.getLastDonators().get(arg - 1).getPrice());
            }
            if (params.contains("last_donator_package_")) {
                int arg = Integer.parseInt(params.replaceFirst("last_donator_package_", ""));
                return data.getLastDonators().get(arg - 1).getPackageName();
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
            if (params.contains("donation_goal_bar")) {
                int amount = Integer.parseInt(params.replaceFirst("donation_goal_bar_", ""));
                Component component = Component.text("");
                if (data.getDonationGoal().getDonationGoalPercentage() > 100) {
                    for (int i = 0; i < amount; i++) {
                        component = component.append(Component.text("█").color(NamedTextColor.GREEN));
                    }
                    LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
                    return serializer.serialize(component);
                }
                if (data.getDonationGoal().getDonationGoalPercentage() < 0) {
                    for (int i = 0; i < amount; i++) {
                        component = component.append(Component.text("█").color(NamedTextColor.RED));
                    }
                    LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
                    return serializer.serialize(component);
                }
                int step = 100 / amount;
                for (int i = 0; i < amount; i++) {
                    if ((i + 1) * step <= data.getDonationGoal().getDonationGoalPercentage()) {
                        component = component.append(Component.text("█").color(NamedTextColor.GREEN));
                        continue;
                    }
                    component = component.append(Component.text("█").color(NamedTextColor.RED));
                }
                LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
                return serializer.serialize(component);
            }
            if (params.contains("player_spent")) {
                String name = p.getName();
                ProfileManager.Profile profile = WebApiAccessor.profileManager().getCachedProfile(name);
                if (profile == null) {
                    return "";
                }
                return String.valueOf(profile.moneySpent());
            }
        } catch (Exception e) {
            plugin.debug("Placeholder error: " + e.getMessage());
            return "";
        }
        return "";
    }
}
