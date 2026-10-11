package me.chrommob.minestore.common.commandHolder;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.api.interfaces.commands.ParsedResponse;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.commandHolder.storage.CommandDumper;
import me.chrommob.minestore.common.commandHolder.storage.NewCommandDumper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;

public class CommandStorageTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private MineStoreCommon plugin;

    @Before
    public void setUp() throws Exception {
        Registries.LOGGER.set(message -> {});
        Registries.CONFIG_FILE.set(new File(folder.getRoot(), "config.yml"));
        plugin = new MineStoreCommon();
        // Normally created in MineStoreCommon.init, which needs a store to talk to.
        setField("commandDumper", new CommandDumper(plugin));
        setField("newCommandDumper", new NewCommandDumper(plugin));
    }

    private void setField(String name, Object value) throws Exception {
        Field field = MineStoreCommon.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(plugin, value);
    }

    private CommandStorage newStorage() {
        CommandStorage storage = new CommandStorage(plugin);
        storage.init();
        return storage;
    }

    private static ParsedResponse command(int id, String command) {
        return new ParsedResponse(ParsedResponse.TYPE.COMMAND, ParsedResponse.COMMAND_TYPE.ONLINE, command, "Steve", id);
    }

    @Test
    public void testRefetchBeforeDeliveryIsNotStoredTwice() {
        CommandStorage storage = newStorage();
        Assert.assertEquals(1, storage.receive(Collections.singletonList(command(5, "give Steve diamond 1"))).size());
        // The delivery was not confirmed yet, so the queue hands out the same command again.
        Assert.assertTrue(storage.receive(Collections.singletonList(command(5, "give Steve diamond 1"))).isEmpty());
        Assert.assertEquals(1, storage.undelivered().size());
    }

    @Test
    public void testResendAfterDeliveryIsStored() {
        CommandStorage storage = newStorage();
        storage.receive(Collections.singletonList(command(5, "give Steve diamond 1")));
        storage.markDelivered(storage.undelivered());
        // Resent from the panel with the same id.
        Assert.assertEquals(1, storage.receive(Collections.singletonList(command(5, "give Steve diamond 1"))).size());
        Assert.assertEquals(1, storage.undelivered().size());
    }

    @Test
    public void testCommandsSurviveRestart() {
        newStorage().receive(Collections.singletonList(command(5, "give Steve diamond 1")));
        CommandStorage restarted = newStorage();
        Assert.assertEquals(1, restarted.undelivered().size());
        Assert.assertEquals("give Steve diamond 1", restarted.undelivered().get(0).command());
    }

    @Test
    public void testSavedCommandsAreMigrated() throws Exception {
        File saved = new File(folder.getRoot(), "savedCommands.json");
        Files.write(saved.toPath(), "{\"steve\":[{\"command\":\"give steve diamond 1\",\"requestId\":9}]}".getBytes(StandardCharsets.UTF_8));
        CommandStorage storage = newStorage();
        Assert.assertTrue(storage.undelivered().isEmpty());
        Assert.assertFalse(saved.exists());
        Assert.assertTrue(new File(folder.getRoot(), "savedCommands.json.migrated").exists());
        String pendingJson = new String(Files.readAllBytes(new File(folder.getRoot(), "pendingCommands.json").toPath()), StandardCharsets.UTF_8);
        Assert.assertTrue(pendingJson.contains("give steve diamond 1"));
        // Migrated commands were delivered already and still wait to be executed.
        storage.markDelivered(storage.undelivered());
        Assert.assertTrue(storage.executedUnconfirmed().isEmpty());
        Assert.assertEquals(1, newStorage().receive(Collections.singletonList(command(9, "give steve diamond 1"))).size());
    }
}
