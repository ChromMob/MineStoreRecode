package me.chrommob.minestore.common.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.command.types.CommonConsoleUser;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.interfaces.user.CommonUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

@CommandAlias("store")
public class StoreCommand extends BaseCommand {
    @Override
    public void help(CommandIssuer issuer, String[] args) {
        CommonUser user = MineStoreCommon.getInstance().userGetter().get(issuer.getUniqueId());
        user.sendMessage(Component.text("[MineStore] /store").color(NamedTextColor.RED));
    }
    @Default
    @CommandPermission("minestore.store")
    @SuppressWarnings("unused")
    public void onStore(final AbstractUser abstractUser) {
        final Component message = (MineStoreCommon.getInstance().miniMessage()).deserialize(((String)MineStoreCommon.getInstance().configReader().get(ConfigKey.STORE_COMMAND_MESSAGE)).replaceAll("%store_url%", (String)MineStoreCommon.getInstance().configReader().get(ConfigKey.STORE_URL)));
        abstractUser.user().sendMessage(message);
    }
}
