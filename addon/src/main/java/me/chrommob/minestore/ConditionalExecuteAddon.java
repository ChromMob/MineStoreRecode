package me.chrommob.minestore;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.event.MineStoreEventBus;
import me.chrommob.minestore.api.event.types.MineStoreExecuteIntentEvent;
import me.chrommob.minestore.api.generic.MineStoreAddon;
import me.chrommob.minestore.api.interfaces.commands.CommonConsoleUser;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

@SuppressWarnings("unused")
public class ConditionalExecuteAddon implements MineStoreAddon {
    public ConditionalExecuteAddon() {
        MineStoreEventBus.registerListener(this, MineStoreExecuteIntentEvent.class, event -> {
            if (!event.command().startsWith("give") && !event.command().startsWith("/give")) {
                return;
            }
            AbstractUser user = Registries.USER_GETTER.get().get(event.username());
            if (user == null) {
                return;
            }
            if (user.commonUser() instanceof CommonConsoleUser) {
                return;
            }
            if (!(user.platformObject() instanceof Player)) {
                return;
            }
            Player player = (Player) user.platformObject();
            int freeSlots = 0;
            ItemStack[] contents = Arrays.copyOf(player.getInventory().getContents(), 36);
            for (ItemStack item : contents) {
                if (item == null || item.getType() == Material.AIR) {
                    freeSlots++;
                }
            }
            if (freeSlots > 0) {
                return;
            }
            user.commonUser().sendMessage("You do not have enough free slots to receive the item!");
            event.setCancelled(true);
        });
    }

    @Override
    public String getName() {
        return "ConditionalExecuteAddon";
    }
}