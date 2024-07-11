package me.chrommob;

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

    @Test
    public void testDisable() {
        MineStoreEventBus.registerListener(MineStoreDisableEvent.class, event -> {
            disabled = true;
        });
        new MineStoreDisableEvent().call();
        Assert.assertTrue(disabled);
    }

    @Test
    public void testEnable() {
        MineStoreEventBus.registerListener(MineStoreEnableEvent.class, event -> {
            enabled = true;
        });
        new MineStoreEnableEvent().call();
        Assert.assertTrue(enabled);
    }

    @Test
    public void testLoad() {
        MineStoreEventBus.registerListener(MineStoreLoadEvent.class, event -> {
            loaded = true;
        });
        new MineStoreLoadEvent().call();
        Assert.assertTrue(loaded);
    }

    @Test
    public void testPurchase() {
        MineStoreEventBus.registerListener(MineStorePurchaseEvent.class, event -> {
            Assert.assertEquals("test", event.username());
            Assert.assertEquals("test", event.command());
            Assert.assertEquals(0, event.id());
            Assert.assertEquals(MineStorePurchaseEvent.COMMAND_TYPE.ONLINE, event.commandType());
            purchase = true;
        });
        new MineStorePurchaseEvent("test", "test", 0, MineStorePurchaseEvent.COMMAND_TYPE.ONLINE).call();
        Assert.assertTrue(purchase);
    }

    @Test
    public void testReload() {
        MineStoreEventBus.registerListener(MineStoreReloadEvent.class, event -> {
            reload = true;
        });
        new MineStoreReloadEvent().call();
        Assert.assertTrue(reload);
    }

    @Test
    public void testCustomEvent() {
        MineStoreEventBus.registerListener(CustomEventExample.class, event -> {
            Assert.assertEquals("test", event.getMessage());
            customEvent = true;
        });
        new CustomEventExample("test").call();
        Assert.assertTrue(customEvent);
    }
}
