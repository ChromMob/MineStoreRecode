package me.chrommob.minestore.common.commands;

import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.stats.BuildConstats;
import me.chrommob.minestore.common.MineStoreCommon;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class VersionCommand {
    @Permission("minestore.version")
    @Command("minestore|ms version")
    public void onVersion(AbstractUser abstractUser) {
        abstractUser.commonUser().sendMessage("MineStore website version: " + MineStoreCommon.version().toString());
        abstractUser.commonUser().sendMessage("MineStore plugin version: " + BuildConstats.VERSION);
    }
}
