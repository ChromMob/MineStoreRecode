package me.chrommob.minestore.common.commands;

import me.chrommob.minestore.api.interfaces.commands.CommonConsoleUser;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.authHolder.AuthUser;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

@SuppressWarnings("unused")
public class AuthCommand {
    private final MineStoreCommon plugin;
    public AuthCommand(MineStoreCommon plugin) {
        this.plugin = plugin;
    }
    @Permission("minestore.auth")
    @Command("minestore|ms auth")
    public void onAuth(AbstractUser abstractUser) {
        CommonUser user = abstractUser.commonUser();
        if (user instanceof CommonConsoleUser) {
            user.sendMessage("[MineStore] You can't use this command from console!");
            return;
        }
        AuthUser authUser = plugin.authHolder().getAuthUser(user.getName().toLowerCase());
        if (authUser == null) {
            user.sendMessage(plugin.miniMessage().deserialize(plugin.pluginConfig().getLang().getKey("auth").getKey("failure-message").getAsString()));
            return;
        }
        authUser.confirmAuth();
        user.sendMessage(plugin.miniMessage().deserialize(plugin.pluginConfig().getLang().getKey("auth").getKey("success-message").getAsString()));
    }
}
