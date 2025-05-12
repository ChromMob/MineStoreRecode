package me.chrommob;

import me.chrommob.minestore.api.event.MineStoreEvent;
import me.chrommob.minestore.api.event.MineStoreEventBus;
import me.chrommob.minestore.api.event.types.*;
import me.chrommob.minestore.api.generic.MineStoreAddon;
import me.chrommob.minestore.api.web.WebApiAccessor;
import me.chrommob.minestore.api.web.giftcard.GiftCardManager;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;

public class EventBusTest {
    private boolean disabled = false;
    private boolean enabled = false;
    private boolean loaded = false;
    private boolean purchase = false;
    private boolean reload = false;
    private boolean customEvent = false;

    private final MineStoreAddon addon = new MineStoreAddon() {
        @Override
        public void onEnable() {

        }

        @Override
        public String getName() {
            return "TestAddon";
        }
    };

    @Test
    public void testDisable() {
        MineStoreEventBus.registerListener(addon, MineStoreDisableEvent.class, event -> {
            disabled = true;
        });
        new MineStoreDisableEvent().call();
        Assert.assertTrue(disabled);
    }

    @Test
    public void testEnable() {
        MineStoreEventBus.registerListener(addon, MineStoreEnableEvent.class, event -> {
            enabled = true;
        });
        new MineStoreEnableEvent().call();
        Assert.assertTrue(enabled);
    }

    @Test
    public void testLoad() {
        MineStoreEventBus.registerListener(addon, MineStoreLoadEvent.class, event -> {
            loaded = true;
        });
        new MineStoreLoadEvent().call();
        Assert.assertTrue(loaded);
    }

    @Test
    public void testPurchase() {
        MineStoreEventBus.registerListener(addon, MineStorePurchaseEvent.class, event -> {
            Assert.assertEquals("test", event.username());
            Assert.assertEquals("test", event.command());
            Assert.assertEquals(0, event.id());
            Assert.assertEquals(MineStoreEvent.COMMAND_TYPE.ONLINE, event.commandType());
            event.setCommandType(MineStoreEvent.COMMAND_TYPE.OFFLINE);
            event.setCommand("test 2");
            purchase = true;
        });
        MineStorePurchaseEvent event = new MineStorePurchaseEvent("test", "test", 0, MineStoreEvent.COMMAND_TYPE.ONLINE);
        event.call();
        Assert.assertEquals("test 2", event.command());
        Assert.assertEquals(MineStoreEvent.COMMAND_TYPE.OFFLINE, event.commandType());
        Assert.assertTrue(purchase);
    }

    @Test
    public void testReload() {
        MineStoreEventBus.registerListener(addon, MineStoreReloadEvent.class, event -> {
            reload = true;
        });
        new MineStoreReloadEvent().call();
        Assert.assertTrue(reload);
    }

    @Test
    public void testCustomEvent() {
        MineStoreEventBus.registerListener(addon, CustomEventExample.class, event -> {
            Assert.assertEquals("test", event.getMessage());
            customEvent = true;
        });
        new CustomEventExample("test").call();
        Assert.assertTrue(customEvent);
    }

    @Test
    public void testAPI() {
        GiftCardManager.CreateGiftCardResponse response = WebApiAccessor.giftCardManager().createGiftCard("test", "test", 100, LocalDateTime.now().plusDays(1), "");
        System.out.println(response.isSuccess());
        System.out.println(response.message());
    }
}
