package me.chrommob.minestore.common.command;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class VersionCommand {
    @Permission("minestore.version")
    @Command("minestore|ms version")
    public void onVersion(AbstractUser abstractUser) {
        abstractUser.user().sendMessage("MineStore version: " + MineStoreCommon.version().toString());
    }
}
