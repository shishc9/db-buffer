package test;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import replacer.LRUReplacer;

public class LRUReplacerTest {

    @Test
    public void test1() {
        LRUReplacer<Integer, Integer> cache = new LRUReplacer<>(5);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        cache.put(4, 4);
        cache.put(5, 5);

        Assert.assertTrue(cache.get(1) == 1 && cache.get(2) == 2 && cache.get(4) == 4);
    }

    @Test
    public void test2() {
        LRUReplacer<Integer, Integer> cache = new LRUReplacer<>(5);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        cache.put(4, 4);
        cache.put(5, 5);

        cache.put(6, 6);

        cache.showLRUList();
        Assert.assertEquals(cache.getMemorySize().intValue(), 5);
    }

    @Test
    public void test3() {
        LRUReplacer<Integer, Integer> cache = new LRUReplacer<>(5);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        cache.put(4, 4);
        cache.put(5, 5);

        cache.put(6, 6);

        cache.put(7, 7);

        cache.showLRUList();
        Assert.assertTrue(cache.get(4) == 4 && cache.get(6) == 6 && cache.get(7) == 7);
    }

    @Test
    public void test4() {
        LRUReplacer<Integer, Integer> cache = new LRUReplacer<>(5);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        cache.put(4, 4);
        cache.put(5, 5);

        cache.put(6, 6);
        cache.put(7, 7);

        Assert.assertTrue(cache.get(1) == null);
    }

    @Test
    public void test5() {
        LRUReplacer<Integer, Integer> cache = new LRUReplacer<>(3);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.showLRUList();
        cache.put(1, 3);
        cache.showLRUList();
        cache.put(3, 3);
        cache.showLRUList();
        cache.put(4, 4);
        cache.showLRUList();
        Assert.assertTrue(cache.get(1) == 3);
    }

}
