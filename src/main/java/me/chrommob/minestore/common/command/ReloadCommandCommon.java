package me.chrommob.minestore.common.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import me.chrommob.minestore.common.MineStoreCommon;
import org.bukkit.command.CommandSender;

@CommandAlias("minestore|ms")
public class ReloadCommandCommon extends BaseCommand {
    @CommandAlias("reload")
    @CommandPermission("minestore.reload|ms.reload")
    @SuppressWarnings("unused")
    public void onReload(CommandSender sender) {
        MineStoreCommon.getInstance().reload();
        sender.sendMessage("[MineStore] Reloaded MineStore...");
    }
}
