package me.chrommob.minestore.common.commands;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.interfaces.commands.CommonConsoleUser;
import me.chrommob.minestore.api.interfaces.user.AbstractUser;
import me.chrommob.minestore.api.interfaces.user.CommonUser;
import me.chrommob.minestore.api.scheduler.MineStoreScheduledTask;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.subsription.SubscriptionUtil;
import me.chrommob.minestore.common.subsription.json.ReturnSubscriptionObject;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

@SuppressWarnings("unused")
public class SubscriptionsCommand {
    private final MineStoreCommon plugin;
    public SubscriptionsCommand(MineStoreCommon plugin) {
        this.plugin = plugin;
    }
    @Permission("minestore.subscriptions")
    @Command("minestore|ms subscriptions")
    public void onSubscription(AbstractUser user) {
        CommonUser commonUser = user.commonUser();
        if (commonUser instanceof CommonConsoleUser) {
            commonUser.sendMessage("[MineStore] You can't use this command from console!");
            return;
        }
        Registries.MINESTORE_SCHEDULER.get().runDelayed(new MineStoreScheduledTask("subscription", () -> {
            ReturnSubscriptionObject returnSubscriptionObject = SubscriptionUtil.getSubscription(commonUser.getName());
            if (returnSubscriptionObject == null) {
                commonUser.sendMessage("[MineStore] The plugin is not successfully connected to the store! Contact the server owner!");
                return;
            }
            commonUser.sendMessage(plugin.miniMessage().deserialize(plugin.pluginConfig().getLang().getKey("subscription").getKey("title").getValueAsString()));
            commonUser.sendMessage(plugin.miniMessage().deserialize(plugin.pluginConfig().getLang().getKey("subscription").getKey("status").getValueAsString().replaceAll("%message%", returnSubscriptionObject.message())));
            if (!returnSubscriptionObject.isSuccess()) {
                return;
            }
            for (String url : returnSubscriptionObject.urls()) {
                commonUser.sendMessage(plugin.miniMessage().deserialize(plugin.pluginConfig().getLang().getKey("subscription").getKey("url").getValueAsString().replaceAll("%url%", url)));
            }
        }, 0));
    }
}
