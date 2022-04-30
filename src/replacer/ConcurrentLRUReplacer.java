package replacer;

import interfac3.Replacer;
import interfac3.PutVO;

import java.util.ArrayList;
import java.util.HashMap;

public class ConcurrentLRUReplacer<K,V> implements Replacer<K,V> {

    /**
     * 《Effective Java 3》 第28条：数组是具化的（reified），数组需要在运行时知道和强化它们的元素类型。
     * 不能创建泛型、参数化类型、类型参数的数组（new Gen<T>[]、new Gen<Integer>[]、 new T[]）这些都是非法的，编译时会导致GAC错误。
     * 从技术角度讲，T、Gen<T>和Gen<Integer>这样的类型应称为不可具化的（nonreifiable）
     * 意思就是：不可具化的类型运行时的描述信息比它编译时包含的信息更少。
     * 唯一可具化的参数化类型是无界通配符：Gen<?>。
     * 建议：在创建泛型数组错误时，可以优先使用集合类型List<E>，而不是数组类型E[].
     * List<Gen<Integer>> gList = new ArrayList<Gen<Integer>>();
     */
    private ArrayList<LRUReplacer<K, V>> cacheSegments;
    private final Integer maxCapacity;

    public ConcurrentLRUReplacer(final int capacity) {
        maxCapacity = capacity;
        int cores = Runtime.getRuntime().availableProcessors();
        int concurrency = Math.max(cores, 4);
        cacheSegments = new ArrayList<>(concurrency);
        int segmentCapacity = capacity / concurrency;
        if (capacity % concurrency > 0) {
            segmentCapacity ++;
        }
        for (int index = 0; index < concurrency; index++) {
            LRUReplacer<K, V> lruReplacer = new LRUReplacer<>(segmentCapacity);
            cacheSegments.add(index, lruReplacer);
        }
    }

    public ConcurrentLRUReplacer(final int concurrency, final int capacity) {
        cacheSegments = new ArrayList<>(concurrency);
        this.maxCapacity = capacity;
        int segmentCapacity = capacity / concurrency;
        if (capacity % concurrency > 0) {
            segmentCapacity ++;
        }
        for (int index = 0; index < concurrency; index++) {
            LRUReplacer<K, V> lruReplacer = new LRUReplacer<>(segmentCapacity);
            cacheSegments.add(index, lruReplacer);
        }
    }

    private int segmentIndex(K key) {
        int hashCode = Math.abs(key.hashCode() * 31);
        return hashCode % cacheSegments.size();
    }

    private LRUReplacer<K, V> getSegmentCacheByIndex(K key) {
        return cacheSegments.get(segmentIndex(key));
    }

    @Override
    public V get(K key) {
        return getSegmentCacheByIndex(key).get(key);
    }

    @Override
    public PutVO put(K key, V value, HashMap<Integer, FrameDescriptor> frameTable) {
        return getSegmentCacheByIndex(key).put(key, value, frameTable);
    }

    @Override
    public V remove(K key, HashMap<Integer, FrameDescriptor> frameTable) {
        return getSegmentCacheByIndex(key).remove(key, frameTable);
    }

    @Override
    public Boolean contains(K key) {
        return getSegmentCacheByIndex(key).contains(key);
    }

    @Override
    public V getWithoutMove(K key) {
        return getSegmentCacheByIndex(key).getWithoutMove(key);
    }

    @Override
    public Integer getMaxMemorySize() {
        return maxCapacity;
    }

    @Override
    public Integer getMemorySize() {
        Integer size = 0;
        for (LRUReplacer<K, V> cache : cacheSegments) {
            size += cache.getMemorySize();
        }
        return size;
    }

    @Override
    public void showReplacerStatus() {
        for (LRUReplacer<K, V> replacer : cacheSegments) {
            replacer.showReplacerStatus();
        }
    }

    @Override
    public void clear() {

    }

    public void showLRUList() {
        for (LRUReplacer<K, V> cache : cacheSegments) {
            cache.showLRUList();
        }
    }
}
