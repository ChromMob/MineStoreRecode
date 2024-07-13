package me.chrommob.minestore.addons.webApi;

import me.chrommob.minestore.addons.events.types.MineStorePurchaseEvent;

public class WebApiAccessor {
    public enum CommandReason {
        PURCHASE,
        SUBSCRIPTION_START,
        SUBSCRIPTION_END
    }
    public static CommandReason getCommandReason(String id, String username) {
        return CommandReason.PURCHASE;
    }

    public static CommandReason getCommandReason(MineStorePurchaseEvent event) {
        return getCommandReason(String.valueOf(event.id()), event.username());
    }
}