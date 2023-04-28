package me.chrommob.minestore.platforms.bungee.webCommand;

import me.chrommob.minestore.common.interfaces.commands.CommandExecuterCommon;
import me.chrommob.minestore.platforms.bungee.MineStoreBungee;

public class CommandExecuterBungee implements CommandExecuterCommon {
    private final MineStoreBungee mineStoreBungee;

    public CommandExecuterBungee(MineStoreBungee mineStoreBungee) {
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
