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
            System.out.println("Command: " + event.command() + " for user: " + event.username());
            if (!event.command().startsWith("give") && !event.command().startsWith("/give")) {
                System.out.println("Not a give command");
                return;
            }
            AbstractUser user = Registries.USER_GETTER.get().get(event.username());
            if (user == null) {
                System.out.println("User is null");
                return;
            }
            if (user.commonUser() instanceof CommonConsoleUser) {
                System.out.println("User is console");
                return;
            }
            if (!(user.platformObject() instanceof Player)) {
                System.out.println("User is not a player");
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
                System.out.println("Free slots: " + freeSlots);
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