package me.chrommob.minestore.platforms.bungee.webCommand;

import me.chrommob.minestore.api.interfaces.commands.CommandExecuterCommon;
import net.md_5.bungee.api.plugin.Plugin;

public class CommandExecuterBungee extends CommandExecuterCommon {
    private final Plugin mineStoreBungee;

    public CommandExecuterBungee(Plugin mineStoreBungee) {
        this.mineStoreBungee = mineStoreBungee;
    }


    @Override
    public void execute(String command) {
        mineStoreBungee.getProxy().getScheduler().schedule(mineStoreBungee, () -> {
            mineStoreBungee.getProxy().getPluginManager().dispatchCommand(mineStoreBungee.getProxy().getConsole(), command);
        }, 1, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isOnline(String username) {
        return mineStoreBungee.getProxy().getPlayer(username) != null;
    }
}
