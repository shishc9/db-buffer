package interfac3;

import replacer.FrameDescriptor;

import java.util.HashMap;

public interface Replacer<K, V> {

    V get(K key);

    V put(K key, V value, HashMap<Integer, FrameDescriptor> frameTable);

    V remove(K key, HashMap<Integer, FrameDescriptor> frameTable);

    Boolean contains(K key);

    V getWithoutMove(K key);

    Integer getMaxMemorySize();

    Integer getMemorySize();

    void clear();
}
