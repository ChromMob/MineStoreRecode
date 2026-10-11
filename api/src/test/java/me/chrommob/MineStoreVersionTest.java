package me.chrommob;

import me.chrommob.minestore.api.generic.MineStoreVersion;
import org.junit.Assert;
import org.junit.Test;

public class MineStoreVersionTest {
    @Test
    public void testPlainVersion() {
        Assert.assertEquals("3.8.3", new MineStoreVersion("3.8.3").toString());
    }

    @Test
    public void testVersionWithSuffix() {
        MineStoreVersion version = new MineStoreVersion("3.8.3-beta");
        Assert.assertEquals("3.8.3-beta", version.toString());
        Assert.assertEquals(MineStoreVersion.Comparison.EQUAL, version.compare(3, 8, 3));
    }
}
