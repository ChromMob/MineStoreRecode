package me.chrommob.minestore.common.command;

import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.command.types.CommonConsoleUser;
import me.chrommob.minestore.common.config.ConfigKey;
import me.chrommob.minestore.common.interfaces.user.AbstractUser;
import me.chrommob.minestore.common.interfaces.user.CommonUser;
import me.chrommob.minestore.common.subsription.SubscriptionUtil;
import me.chrommob.minestore.common.subsription.json.ReturnSubscriptionObject;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class SubscriptionsCommand {
    private final MineStoreCommon plugin;
    public SubscriptionsCommand(MineStoreCommon plugin) {
        this.plugin = plugin;
    }
    @Permission("minestore.subscriptions")
    @Command("minestore|ms subscriptions")
    public void onSubscription(AbstractUser user) {
        CommonUser commonUser = user.user();
        if (commonUser instanceof CommonConsoleUser) {
            commonUser.sendMessage("[MineStore] You can't use this command from console!");
            return;
        }
        ReturnSubscriptionObject returnSubscriptionObject = SubscriptionUtil.getSubscription(commonUser.getName());
        if (returnSubscriptionObject == null) {
            commonUser.sendMessage("[MineStore] The plugin is not successfully connected to the store! Contact the server owner!");
            return;
        }
        commonUser.sendMessage(plugin.miniMessage().deserialize(((String)plugin.configReader().get(ConfigKey.SUBSCRIPTION_TITLE))));
        commonUser.sendMessage(plugin.miniMessage().deserialize(((String)plugin.configReader().get(ConfigKey.SUBSCRIPTION_MESSAGE)).replaceAll("%message%", returnSubscriptionObject.message())));
        if (!returnSubscriptionObject.isSuccess()) {
            return;
        }
        for (String url : returnSubscriptionObject.urls()) {
            commonUser.sendMessage(plugin.miniMessage().deserialize(((String)plugin.configReader().get(ConfigKey.SUBSCRIPTION_URL)).replaceAll("%url%", url)));
        }
    }
}