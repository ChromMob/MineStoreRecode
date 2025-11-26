package me.chrommob.minestore;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.event.MineStoreEventBus;
import me.chrommob.minestore.api.event.types.MineStoreExecuteIntentEvent;
import me.chrommob.minestore.api.event.types.MineStoreLoadEvent;
import me.chrommob.minestore.api.generic.MineStoreAddon;
import me.chrommob.minestore.api.interfaces.commands.CommonConsoleUser;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.libs.me.chrommob.config.ConfigManager.ConfigKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings("unused")
public class ConditionalExecuteAddon extends MineStoreAddon {
    private static final Function<Component, String> serialize = c -> MiniMessage.miniMessage().serialize(c);
    private static final Function<String, Component> deserialize = s -> MiniMessage.miniMessage().deserialize(s);


    private static final ConfigKey<Component> MESSAGE;
    private static final ConfigKey<Component> INIT_MESSAGE;
    static {
        MESSAGE = new ConfigKey<>("message", Component.text("You do not have enough free slots to receive the item!").color(NamedTextColor.RED), serialize, deserialize);
        INIT_MESSAGE = new ConfigKey<>("init_message", Component.text("ConditionalExecuteAddon").decorate(TextDecoration.BOLD).color(NamedTextColor.RED).append(Component.text(" has been enabled!")), serialize, deserialize);
    }

    @Override
    public void onEnable() {
        MineStoreEventBus.registerListener(this, MineStoreLoadEvent.class, mineStoreLoadEvent -> {
            Registries.LOGGER.get().log(PlainTextComponentSerializer.plainText().serialize(INIT_MESSAGE.getValue()));
        });
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
            user.commonUser().sendMessage(MESSAGE.getValue());
            event.setCancelled(true);
        });
    }

    @Override
    public String getName() {
        return "ConditionalExecuteAddon";
    }

    @Override
    public List<Object> getCommands() {
        List<Object> list = new ArrayList<>();
        list.add(new Test());
        return list;
    }

    @Override
    public List<ConfigKey<?>> getConfigKeys() {
        List<ConfigKey<?>> keys = new ArrayList<>();
        keys.add(MESSAGE);
        keys.add(INIT_MESSAGE);
        return keys;
    }

    private static class Test {
        @Command("test <value>")
        public void test(AbstractUser abstractUser, @Argument("value") boolean value) {
            abstractUser.commonUser().sendMessage("GG " + value);
        }
    }
}