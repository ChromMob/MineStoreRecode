package me.chrommob.minestore.common.commands;

import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.common.MineStoreCommon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

@SuppressWarnings("unused")
public class DumpCommand {
    private final MineStoreCommon plugin;
    public DumpCommand(MineStoreCommon plugin) {
        this.plugin = plugin;
    }
    @Permission("minestore.dump")
    @Command("minestore|ms dump")
    public void onDumpCommand(AbstractUser abstractUser) {
        CommonUser user = abstractUser.user();
        user.sendMessage(Component.text("Dumping MineStore data...").color(NamedTextColor.GREEN));
        new Thread(() -> {
            String link = plugin.dumper().dump(true, plugin);
            user.sendMessage(Component.text("Dumped MineStore data to ").color(NamedTextColor.GRAY)
                    .append(Component.text(link).color(NamedTextColor.YELLOW)
                            .append(Component.text(" (Click to copy)").color(NamedTextColor.GOLD)
                                    .clickEvent(ClickEvent.copyToClipboard(link)))));
        }).start();
    }
}
