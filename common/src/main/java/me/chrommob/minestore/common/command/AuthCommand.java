package me.chrommob.minestore.common.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.authHolder.AuthUser;
import me.chrommob.minestore.common.command.types.CommonConsoleUser;
import me.chrommob.minestore.common.command.types.MineStoreCommand;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.interfaces.user.CommonUser;

@CommandAlias("minestore|ms")
public class AuthCommand extends MineStoreCommand {
    @CommandPermission("minestore.auth")
    @Subcommand("auth")
    @SuppressWarnings("unused")
    public void onAuth(AbstractUser abstractUser) {
        CommonUser user = abstractUser.user();
        if (user instanceof CommonConsoleUser) {
            user.sendMessage("[MineStore] You can't use this command from console!");
            return;
        }
        AuthUser authUser = MineStoreCommon.getInstance().authHolder().getAuthUser(user.getName());
        if (authUser == null) {
            user.sendMessage((MineStoreCommon.getInstance().miniMessage()).deserialize((String)MineStoreCommon.getInstance().configReader().get(ConfigKey.AUTH_FAILURE_MESSAGE)));
            return;
        }
        authUser.confirmAuth();
        user.sendMessage((MineStoreCommon.getInstance().miniMessage()).deserialize((String)MineStoreCommon.getInstance().configReader().get(ConfigKey.AUTH_SUCCESS_MESSAGE)));
    }
}
