package me.chrommob.minestore.platforms.bukkit.events;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.interfaces.gui.CommonItem;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.platforms.bukkit.MineStoreBukkit;
import net.kyori.adventure.platform.bukkit.BukkitComponentSerializer;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class BukkitInventoryEvent implements Listener {
    private final MineStoreCommon plugin;
    public BukkitInventoryEvent(MineStoreBukkit plugin, MineStoreCommon pl) {
        this.plugin = pl;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String eventTitle = event.getView().getTitle();
        if (eventTitle == null) return;
        if (event.getCurrentItem() == null) return;
        boolean isMineStoreGui = false;
        if (plugin.guiData() == null || plugin.guiData().getGuiInfo() == null || plugin.guiData().getGuiInfo().getTitles() == null) return;
        for (Component title : plugin.guiData().getGuiInfo().getTitles()) {
            String titleString = BukkitComponentSerializer.legacy().serialize(title);
            if (eventTitle.equals(titleString)) {
                isMineStoreGui = true;
                event.setCancelled(true);
                break;
            }
        }
        if (!isMineStoreGui) {
            for (Component title : plugin.guiData().getGuiInfo().getCustomTitles()) {
                String titleString = BukkitComponentSerializer.legacy().serialize(title);
                if (eventTitle.equals(titleString)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        if (!isMineStoreGui) return;
        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getItemMeta() == null) return;
        Component name = BukkitComponentSerializer.legacy().deserialize(event.getCurrentItem().getItemMeta().getDisplayName());
        List<Component> lore = new ArrayList<>();
        if (event.getCurrentItem().getItemMeta().getLore() != null) {
            for (String line : event.getCurrentItem().getItemMeta().getLore()) {
                lore.add(BukkitComponentSerializer.legacy().deserialize(line));
            }
        }
        CommonItem item = new CommonItem(name, event.getCurrentItem().getType().toString(), lore);
        plugin.guiData().getGuiInfo().handleInventoryClick(Registries.USER_GETTER.get().get(event.getWhoClicked().getUniqueId()), item);
    }
}
