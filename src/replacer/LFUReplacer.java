package replacer;

import interfac3.Replacer;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LFUReplacer<K, V> implements Replacer<K, V> {

    private HashMap<K, LFUNode<K, V>> cache;
    private HashMap<Integer, LinkedHashSet<LFUNode<K, V>>> freqMap;
    private Integer size;
    private Integer minFreq;
    private Integer capacity;
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Lock writeLock = lock.writeLock();
    private Lock readLock = lock.readLock();

    public LFUReplacer(Integer capacity) {
        this.capacity = capacity;
        cache = new HashMap<>(capacity);
        freqMap = new HashMap<>();
    }

    private void addNode(LFUNode<K, V> node) {
        LinkedHashSet<LFUNode<K, V>> set = freqMap.get(1);
        if (set == null) {
            set = new LinkedHashSet<>();
            freqMap.put(1, set);
        }
        set.add(node);
        minFreq = 1;
    }

    private LFUNode<K, V> removeNode() {
        LinkedHashSet<LFUNode<K, V>> set = freqMap.get(minFreq);
        LFUNode<K, V> deadNode = set.iterator().next();
        set.remove(deadNode);
        return deadNode;
    }

    @Override
    public V get(K key) {
        writeLock.lock();
        try {
            LFUNode<K, V> node = cache.get(key);
            if (node == null) {
                return null;
            }
            freqInc(node);
            return node.value;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        writeLock.lock();
        try {
            if (capacity == 0) {
                return null;
            }
            LFUNode<K, V> node = cache.get(key);
            if (node != null) {
                node.value = value;
                freqInc(node);
            } else {
                if (Objects.equals(size, capacity)) {
                    LFUNode<K, V> deadNode = removeNode();
                    cache.remove(deadNode.key);
                    size --;
                }
                LFUNode<K, V> newNode = new LFUNode<>(key, value);
                cache.put(key, newNode);
                addNode(newNode);
                size ++;
            }
            return value;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Boolean contains(K key) {
        return cache.containsKey(key);
    }

    private void freqInc(LFUNode<K, V> node) {
        int freq = node.freq;
        LinkedHashSet<LFUNode<K, V>> set = freqMap.get(freq);
        set.remove(node);
        if (freq == minFreq && set.size() == 0) {
            minFreq = freq + 1;
        }
        node.freq ++;
        LinkedHashSet<LFUNode<K, V>> newSet = freqMap.get(node.freq);
        if (newSet == null) {
            newSet = new LinkedHashSet<>();
            freqMap.put(node.freq, newSet);
        }
        newSet.add(node);
    }

    @Override
    public Integer getMaxMemorySize() {
        return capacity;
    }

    @Override
    public Integer getMemorySize() {
        readLock.lock();
        try {
            return cache.size();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void clear() {

    }

    public void showLFUList() {
        ArrayList<String> resultList = new ArrayList<>();
        for (Map.Entry<K, LFUNode<K, V>> entry : cache.entrySet()) {
            resultList.add(entry.getValue().toString());
        }
        System.out.println(resultList);
    }

    public static class LFUNode<K, V> {
        K key;
        V value;
        Integer freq = 1;

        public LFUNode() {}
        public LFUNode(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "key:" + key + " value:" + value + " freq:" + freq;
        }
    }
}
