package me.chrommob.minestore.common.placeholder;

import org.junit.Assert;
import org.junit.Test;

public class IndexedBlockDequeTest {
    @Test
    public void testPushLastPastRootBlock() {
        IndexedBlockDeque<Integer> deque = new IndexedBlockDeque<>(4);
        for (int i = 0; i < 30; i++) {
            deque.pushLast(i);
            for (int j = 0; j <= i; j++) {
                Assert.assertEquals("after " + (i + 1) + " pushes, index " + j, Integer.valueOf(j), deque.get(j));
            }
        }
        Assert.assertEquals(30, deque.size());
    }

    @Test
    public void testPushFirstPastRootBlock() {
        IndexedBlockDeque<Integer> deque = new IndexedBlockDeque<>(4);
        for (int i = 0; i < 30; i++) {
            deque.pushFirst(i);
            for (int j = 0; j <= i; j++) {
                Assert.assertEquals("after " + (i + 1) + " pushes, index " + j, Integer.valueOf(i - j), deque.get(j));
            }
        }
    }
}
