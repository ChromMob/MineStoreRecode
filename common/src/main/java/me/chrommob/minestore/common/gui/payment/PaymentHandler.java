package me.chrommob.minestore.common.gui.payment;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.commands.ChargeBalanceCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PaymentHandler {
    private final MineStoreCommon plugin;
    private final Map<String, Set<String>> payments = new ConcurrentHashMap<>();
    public PaymentHandler(MineStoreCommon plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Boolean> createPayment(String username, int itemId) {
        return plugin.webListener().createPayment(username, itemId).thenApply(paymentCreationResponse -> {
            if (!paymentCreationResponse.isSuccess()) {
                plugin.log("Failed to create payment: " + paymentCreationResponse.getMessage());
                return false;
            }
            if (!paymentCreationResponse.getPaymentResponse().isSuccess()) {
                plugin.log("Failed to create payment: " + paymentCreationResponse.getPaymentResponse().getData().getUrl());
                return false;
            }
            String orderId = paymentCreationResponse.getPaymentResponse().getData().getOrderId();
            payments.compute(username.toLowerCase(), (k, v) -> {
                if (v == null) {
                    v = new HashSet<>();
                }
                v.add(orderId);
                return v;
            });
            plugin.debug(this.getClass(), "Created payment for " + username + " with order id: " + orderId);
            return true;
        });
    }

    public void handlePayment(ChargeBalanceCommand.ResponseData responseData) {
        String username = responseData.data.username;
        String paymentInternalId = responseData.data.payment_internal_id;
        Set<String> orderIds = payments.get(username.toLowerCase());
        if (orderIds == null || orderIds.isEmpty() || !orderIds.contains(paymentInternalId)) {
            plugin.debug(this.getClass(), "Payment for " + username + " with id " + paymentInternalId + " was not made in-game");
            return;
        }
        if (responseData.status.equals("success")) {
            Registries.USER_GETTER.get().get(username).sendMessage(Component.text("You have successfully bought the item for " + responseData.data.price + "!").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
        } else {
            Registries.USER_GETTER.get().get(username).sendMessage(Component.text("Failed to buy the item! You do not have enough money!").color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
        }
    }
}
