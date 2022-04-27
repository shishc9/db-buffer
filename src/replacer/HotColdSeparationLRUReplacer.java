package replacer;

import interfac3.Replacer;
import interfac3.PutVO;

import java.util.HashMap;

public class HotColdSeparationLRUReplacer<K, V> implements Replacer<K, V> {



    @Override
    public V get(K key) {
        return null;
    }

    @Override
    public PutVO put(K key, V value, HashMap<Integer, FrameDescriptor> frameTable) {
        return null;
    }

    @Override
    public V remove(K key, HashMap<Integer, FrameDescriptor> frameTable) {
        return null;
    }

//    @Override
//    public V put(K key, V value) {
//        return null;
//    }

    @Override
    public Boolean contains(K key) {
        return null;
    }

    @Override
    public V getWithoutMove(K key) {
        return null;
    }

    @Override
    public Integer getMaxMemorySize() {
        return null;
    }

    @Override
    public Integer getMemorySize() {
        return null;
    }

    @Override
    public void clear() {

    }
}
