package me.chrommob.minestore.platforms.hytale.command;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import me.chrommob.minestore.api.interfaces.commands.CommandExecuterCommon;

public class HytaleCommandExecuter extends CommandExecuterCommon {

    @Override
    public void execute(String s) {
        HytaleServer.get().getCommandManager().handleCommand(ConsoleSender.INSTANCE, s);
    }

    @Override
    public boolean isOnline(String s) {
        PlayerRef player = Universe.get().getPlayerByUsername(s, NameMatching.EXACT_IGNORE_CASE);
        return player != null && player.isValid();
    }
}
