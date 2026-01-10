package me.chrommob.minestore.platforms.bukkit.events;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.event.types.GuiClickEvent;
import me.chrommob.minestore.api.interfaces.gui.CommonInventory;
import me.chrommob.minestore.api.interfaces.gui.CommonItem;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.platforms.bukkit.user.MineStoreInventoryHolder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class BukkitInventoryEvent implements Listener {
    private final MineStoreCommon common;

    public BukkitInventoryEvent(org.bukkit.plugin.java.JavaPlugin plugin, MineStoreCommon common) {
        this.common = common;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();

        if (!(holder instanceof MineStoreInventoryHolder)) {
            return;
        }

        event.setCancelled(true);

        CommonInventory inventory = ((MineStoreInventoryHolder) holder).getCommonInventory();
        CommonUser user = Registries.USER_GETTER.get().get(event.getWhoClicked().getUniqueId()).commonUser();

        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= inventory.getItems().size()) {
            return;
        }

        CommonItem clickedItem = inventory.getItems().get(rawSlot);
        if (clickedItem == null || !clickedItem.hasClickHandler()) {
            return;
        }

        GuiClickEvent guiEvent = new GuiClickEvent(user, clickedItem, inventory);
        clickedItem.invokeClickHandler(guiEvent);

        if (guiEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }
}
