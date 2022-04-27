package interfac3;

import replacer.FrameDescriptor;

import java.util.HashMap;

public interface Replacer<K, V> {

    /**
     * 在replacer中获取key对应value.
     */
    V get(K key);

    /**
     * 在缓存中插入一个键值对. 如果发生了页面替换，
     */
    PutVO put(K key, V value, HashMap<Integer, FrameDescriptor> frameTable);

    V remove(K key, HashMap<Integer, FrameDescriptor> frameTable);

    Boolean contains(K key);

    V getWithoutMove(K key);

    Integer getMaxMemorySize();

    Integer getMemorySize();

    void clear();
}
