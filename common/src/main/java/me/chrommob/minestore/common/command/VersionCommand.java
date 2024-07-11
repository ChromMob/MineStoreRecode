package me.chrommob.minestore.common.command;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.config.MineStoreVersion;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class VersionCommand {
    private final MineStoreCommon plugin;
    public VersionCommand(MineStoreCommon plugin) {
        this.plugin = plugin;
    }
    @Permission("minestore.version")
    @Command("minestore|ms version")
    public void onVersion(AbstractUser abstractUser) {
        MineStoreVersion version = plugin.version();
        abstractUser.user().sendMessage("MineStore version: " + version.toString());
    }
}
