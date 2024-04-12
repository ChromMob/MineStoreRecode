package me.chrommob.minestore.common.command;

import cloud.commandframework.annotations.Command;
import cloud.commandframework.annotations.Permission;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.interfaces.user.CommonUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

@SuppressWarnings("unused")
public class DumpCommand {
    @Permission("minestore.dump")
    @Command("minestore|ms dump")
    public void onDumpCommand(AbstractUser abstractUser) {
        CommonUser user = abstractUser.user();
        user.sendMessage(Component.text("Dumping MineStore data...").color(NamedTextColor.GREEN));
        new Thread(() -> {
            String link = MineStoreCommon.getInstance().dumper().dump(true);
            user.sendMessage(Component.text("Dumped MineStore data to ").color(NamedTextColor.GRAY)
                    .append(Component.text(link).color(NamedTextColor.YELLOW)
                            .append(Component.text(" (Click to copy)").color(NamedTextColor.GOLD)
                                    .clickEvent(ClickEvent.copyToClipboard(link)))));
        }).start();
    }
}
