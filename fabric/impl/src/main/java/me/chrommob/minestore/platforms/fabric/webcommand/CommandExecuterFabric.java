package me.chrommob.minestore.platforms.fabric.webcommand;

import me.chrommob.minestore.api.interfaces.commands.CommandExecuterCommon;
import me.chrommob.minestore.platforms.fabric.MineStoreFabric;
import net.minecraft.server.MinecraftServer;

public class CommandExecuterFabric extends CommandExecuterCommon {
    private final MinecraftServer server;

    public CommandExecuterFabric(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void execute(String command) {
        try {
            MineStoreFabric.getInstance().getCommon().runOnMainThread(
                    () -> server.getCommandManager().executeWithPrefix(server.getCommandSource(), command));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isOnline(String username) {
        return server.getPlayerManager().getPlayer(username) != null;
    }

}
