package me.chrommob.minestore.velocity.webCommand;

import com.velocitypowered.api.proxy.ProxyServer;
import me.chrommob.minestore.common.templates.CommandExecuterCommon;

public class CommandExecuterVelocity implements CommandExecuterCommon {
    private final ProxyServer server;
    public CommandExecuterVelocity(ProxyServer server) {
        this.server = server;
    }
    @Override
    public void execute(String command) {
        server.getCommandManager().executeAsync(server.getConsoleCommandSource(), command);
    }

    @Override
    public boolean isOnline(String username) {
        return server.getPlayer(username).isPresent();
    }
}
