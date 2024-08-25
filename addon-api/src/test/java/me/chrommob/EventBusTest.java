package me.chrommob;

import me.chrommob.minestore.addons.MineStoreAddon;
import me.chrommob.minestore.addons.events.*;
import me.chrommob.minestore.addons.events.types.*;
import org.junit.Assert;
import org.junit.Test;

public class EventBusTest {
    private boolean disabled = false;
    private boolean enabled = false;
    private boolean loaded = false;
    private boolean purchase = false;
    private boolean reload = false;
    private boolean customEvent = false;

    private MineStoreAddon addon = () -> "test";

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
        MineStoreEventBus.registerListener(addon,MineStoreEnableEvent.class, event -> {
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
            event.setCancelled(true);
            Assert.assertEquals("test", event.username());
            Assert.assertEquals("test", event.command());
            Assert.assertEquals(0, event.id());
            Assert.assertEquals(MineStorePurchaseEvent.COMMAND_TYPE.ONLINE, event.commandType());
            event.setCommandType(MineStorePurchaseEvent.COMMAND_TYPE.OFFLINE);
            event.setCommand("test 2");
            purchase = true;
        });
        MineStorePurchaseEvent event = new MineStorePurchaseEvent("test", "test", 0, MineStorePurchaseEvent.COMMAND_TYPE.ONLINE);
        event.call();
        if (!event.isCancelled()) {
            Assert.fail("Event should be cancelled");
        }
        Assert.assertEquals("test 2", event.command());
        Assert.assertEquals(MineStorePurchaseEvent.COMMAND_TYPE.OFFLINE, event.commandType());
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
}
