package me.chrommob.minestore.platforms.sponge.webCommand;

import me.chrommob.minestore.common.interfaces.commands.CommandExecuterCommon;
import org.spongepowered.api.Sponge;

public class CommandExecuterSponge implements CommandExecuterCommon {

    @Override
    public void execute(String command) {
        Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command);
    }

    @Override
    public boolean isOnline(String username) {
        return Sponge.getServer().getPlayer(username).isPresent();
    }
}
