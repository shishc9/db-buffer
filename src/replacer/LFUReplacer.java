package replacer;

import interfac3.Replacer;
import interfac3.PutVO;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LFUReplacer<K, V> implements Replacer<K, V> {

    private HashMap<K, LFUNode<K, V>> cache;
    private HashMap<Integer, LinkedHashSet<LFUNode<K, V>>> freqMap;
    private Integer minFreq;
    private Integer capacity;
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Lock writeLock = lock.writeLock();
    private Lock readLock = lock.readLock();

    public Integer getMinFreq() {
        return this.minFreq;
    }

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

    private LFUNode<K, V> removeNode(HashMap<Integer, FrameDescriptor> frameTable) {
        // 对freqMap的freq进行排序，因为需要剔除最小的freq节点.
        Integer[] arr = new Integer[freqMap.size()];
        freqMap.keySet().toArray(arr);
        Arrays.sort(arr);
        LFUNode<K, V> deadNode = null;
        for (Integer freq : arr) {
            LinkedHashSet<LFUNode<K, V>> lfuNodes = freqMap.get(freq);
            for (LFUNode<K, V> node : lfuNodes) {
                // 选择freq最小且未被固定的.
                if (!frameTable.get(node.key).isPinned()) {
                    deadNode = node;
                    break;
                }
            }
            if (deadNode != null) {
                break;
            }
        }

        if (deadNode != null) {
            LinkedHashSet<LFUNode<K, V>> set = freqMap.get(deadNode.freq);
            // freqMap和cache都删除节点.
            set.remove(deadNode);
            cache.remove(deadNode.key);
            Integer freq = deadNode.freq;
            // 该节点被删除 需要处理该节点对minFreq的可能影响.
            if (freq == minFreq && set.size() == 0) {
                if (cache.size() == 0) {
                    minFreq = 0;
                } else {
                    minFreq = freq + 1;
                }
            }
            return deadNode;
        }
        return null;
    }

    /**
     * 获取LFU的节点.
     */
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
    public PutVO put(K key, V value, HashMap<Integer, FrameDescriptor> frameTable) {
        writeLock.lock();
        try {
            PutVO putVO;
            // 参数检验
            if (capacity == 0) {
                return null;
            }
            LFUNode<K, V> node = cache.get(key);
            if (node != null) {
                // 该节点在缓存池中，增加其出现的频率.
                node.value = value;
                freqInc(node);
                putVO = new PutVO<>(null, null, "KEY_IN_POOL");
                return putVO;
            } else {
                // 不在缓存池中，缓存已满，进行替换.
                if (Objects.equals(cache.size(), capacity)) {
                    LFUNode<K, V> deadNode = removeNode(frameTable);
                    putVO = new PutVO<>(deadNode.key, deadNode.value, null);
                    LFUNode<K, V> newNode = new LFUNode<>(key, value);
                    cache.put(key, newNode);
                    addNode(newNode);
                    return putVO;
                }
                // 缓存未满 直接添加.
                LFUNode<K, V> newNode = new LFUNode<>(key, value);
                cache.put(key, newNode);
                addNode(newNode);
                putVO = new PutVO<>(null, null, "ADD_NODE");
                return putVO;
            }
        } finally {
            writeLock.unlock();
        }
    }
    
    @Override
    public V remove(K key, HashMap<Integer, FrameDescriptor> frameTable) {
        if (cache.containsKey(key) && frameTable.containsKey(key)) {
            if (!frameTable.get(key).isPinned() && frameTable.get(key).getPinCount().intValue() == 0) {
                LFUNode<K, V> node = cache.get(key);
                cache.remove(key, cache.get(key));
                int freq = node.freq;
                LinkedHashSet<LFUNode<K, V>> set = freqMap.get(freq);
                set.remove(node);
                if (freq == minFreq && set.size() == 0) {
                    if (cache.size() == 0) {
                        minFreq = 0;
                    } else {
                        minFreq = freq + 1;
                    }
                }
                return node.value;
            }
        }
        return null;
    }

    @Override
    public Boolean contains(K key) {
        return cache.containsKey(key);
    }

    @Override
    public V getWithoutMove(K key) {
        return cache.get(key).value;
    }

    /**
     * node的freq增加一次.
     */
    private void freqInc(LFUNode<K, V> node) {
        int freq = node.freq;
        LinkedHashSet<LFUNode<K, V>> set = freqMap.get(freq);
        set.remove(node);
        // 如果当前node.freq是minFreq并且set中无其他元素，则将其加1.
        // 如果全为空了 minFreq可能要归为0. - remove(). => 只能在上层调用
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
    public void showReplacerStatus() {
        showLFUList();
        showFreqList();
    }

    @Override
    public void clear() {

    }

    public void showLFUList() {
        System.out.println("LFUList:");
//        ArrayList<String> resultList = new ArrayList<>();
        for (Map.Entry<K, LFUNode<K, V>> entry : cache.entrySet()) {
            System.out.println(entry.getValue().toString());
//            resultList.add(entry.getValue().toString());
        }
//        System.out.println(resultList);
    }

    public void showFreqList() {
        System.out.println("freqMap:" + freqMap.toString());
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
