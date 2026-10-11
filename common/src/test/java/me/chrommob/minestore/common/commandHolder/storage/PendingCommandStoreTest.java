package me.chrommob.minestore.common.commandHolder.storage;

import me.chrommob.minestore.api.Registries;
import me.chrommob.minestore.common.MineStoreCommon;
import me.chrommob.minestore.common.commandHolder.type.PendingCommand;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class PendingCommandStoreTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private PendingCommandStore store;
    private File file;

    @Before
    public void setUp() {
        Registries.LOGGER.set(message -> {});
        Registries.CONFIG_FILE.set(new File(folder.getRoot(), "config.yml"));
        store = new PendingCommandStore(new MineStoreCommon());
        file = new File(folder.getRoot(), "pendingCommands.json");
    }

    @Test
    public void testRoundTrip() {
        PendingCommand delivered = new PendingCommand(7, "Steve", "give Steve diamond 1", true, true);
        delivered.markExecuted();
        PendingCommand fresh = new PendingCommand(7, "Alex", "say hi", false, false);
        store.save(Arrays.asList(delivered, fresh));

        List<PendingCommand> loaded = store.load();
        Assert.assertEquals(2, loaded.size());
        Assert.assertEquals(delivered.key(), loaded.get(0).key());
        Assert.assertEquals("Steve", loaded.get(0).username());
        Assert.assertTrue(loaded.get(0).isDelivered());
        Assert.assertTrue(loaded.get(0).isExecuted());
        Assert.assertTrue(loaded.get(0).onlineRequired());
        // Same store id, but a separate entry.
        Assert.assertEquals(7, loaded.get(1).id());
        Assert.assertNotEquals(loaded.get(0).key(), loaded.get(1).key());
        Assert.assertFalse(loaded.get(1).isDelivered());
        Assert.assertFalse(new File(folder.getRoot(), "pendingCommands.json.tmp").exists());
    }

    @Test
    public void testEmptyFileLoadsNoCommands() throws Exception {
        Files.write(file.toPath(), new byte[0]);
        Assert.assertTrue(store.load().isEmpty());
    }

    @Test
    public void testCorruptFileIsMovedAside() throws Exception {
        Files.write(file.toPath(), "{not json".getBytes(StandardCharsets.UTF_8));
        Assert.assertTrue(store.load().isEmpty());
        Assert.assertFalse(file.exists());
        File[] backups = folder.getRoot().listFiles((dir, name) -> name.startsWith("pendingCommands.json.corrupt-"));
        Assert.assertNotNull(backups);
        Assert.assertEquals(1, backups.length);
        Assert.assertEquals("{not json", new String(Files.readAllBytes(backups[0].toPath()), StandardCharsets.UTF_8));
    }
}
