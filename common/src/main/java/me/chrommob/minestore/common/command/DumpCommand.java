package me.chrommob.minestore.common.command;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.command.types.MineStoreCommand;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.interfaces.user.CommonUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

@CommandAlias("minestore|ms")
@CommandPermission("minestore.dump")
public class DumpCommand extends MineStoreCommand {

    @CommandAlias("dump")
    @SuppressWarnings("unused")
    public void onDumpCommand(AbstractUser abstractUser) {
        CommonUser user = abstractUser.user();
        user.sendMessage(Component.text("Dumping MineStore data...").color(NamedTextColor.GREEN));
        new Thread(() -> {
            String link = MineStoreCommon.getInstance().dumper().dump();
            user.sendMessage(Component.text("Dumped MineStore data to ").color(NamedTextColor.GRAY)
                    .append(Component.text(link).color(NamedTextColor.YELLOW)
                            .append(Component.text(" (Click to copy)").color(NamedTextColor.GOLD)
                                    .clickEvent(ClickEvent.copyToClipboard(link)))));
        }).start();
    }
}
