package me.chrommob.minestore.platforms.fabric.webcommand;

import me.chrommob.minestore.common.interfaces.commands.CommandExecuterCommon;
import me.chrommob.minestore.platforms.fabric.MineStoreFabric;
import net.minecraft.server.MinecraftServer;

public class CommandExecuterFabric implements CommandExecuterCommon {
    private final MinecraftServer server;

    public CommandExecuterFabric(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void execute(String command) {
        MineStoreFabric.getInstance().getCommon().runOnMainThread(
                () -> server.getCommandManager().executeWithPrefix(server.getCommandSource(), command));
    }

    @Override
    public boolean isOnline(String username) {
        return server.getPlayerManager().getPlayer(username) != null;
    }

}
