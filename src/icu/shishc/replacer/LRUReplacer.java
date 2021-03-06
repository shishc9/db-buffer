package icu.shishc.replacer;

import icu.shishc.interfac3.Replacer;
import icu.shishc.interfac3.PutVO;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LRUReplacer<K, V> implements Replacer<K, V> {

    private final Integer maxCapacity;
    private Map<K, Node<K, V>> map;
    private Node<K, V> head, tail;

    private AtomicInteger ioCount;
    private AtomicInteger ioHitCount;

    public AtomicInteger getIoHitCount() {
        return ioHitCount;
    }

    public void setIoHitCount(AtomicInteger ioHitCount) {
        this.ioHitCount = ioHitCount;
    }

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public LRUReplacer(int initCapacity) {
        this.maxCapacity = initCapacity;
        this.ioCount = new AtomicInteger(0);
        this.ioHitCount = new AtomicInteger(0);
        map = new HashMap<>();
    }

    private void addNode(Node<K, V> node) {
        if (node == null) {
            return;
        }
        if (head == null) {
            head = tail = node;
        } else {
            tail.next = node;
            node.pre = tail;
            node.next = null;
            tail = node;
        }
    }

    private void removeNode(Node<K, V> node) {
        if (node == null) {
            return;
        }
        if (node.pre != null) {
            node.pre.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.pre = node.pre;
        } else {
            tail = node.pre;
        }
    }

    /**
     * 访问了key对应value并发生移动.
     */
    @Override
    public V get(K key) {
        writeLock.lock();
        try {
            Node<K, V> node = map.get(key);
            if (node == null) {
                return null;
            }
            removeNode(node);
            addNode(node);
//            ioHitCount.incrementAndGet();
            return node.value;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * LRU添加一个键值对.
     */
    @Override
    public PutVO put(K key, V value, HashMap<Integer, FrameDescriptor> frameTable) {
        writeLock.lock();
        try {
            PutVO putVO;
            // 如果当前页在缓存中，更新其在缓存中的位置，并将其页面内容刷新，将其标记为脏页.
            if (map.containsKey(key)) {
                Node<K, V> node = map.get(key);
                node.value = value;
                removeNode(node);
                addNode(node);
                ioHitCount.incrementAndGet();
                return new PutVO<>(null, null, "KEY_IN_POOL");
            }
            // 如果缓存空间已满，则需要发生页面替换.
            if (getMemorySize() >= maxCapacity) {
                Node<K, V> cur = head;
//                while (frameTable.get(cur.key).isPinned()) {
//                    cur = cur.next;
//                }
                map.remove(cur.key);
                removeNode(cur);
                // 返回被替换的页面和page.
                putVO = new PutVO<>(cur.key, cur.value, null);
                Node<K, V> node = new Node<>(key, value);
                addNode(node);
                map.put(key, node);
                return putVO;
            }
            Node<K, V> node = new Node<>(key, value);
            addNode(node);
            map.put(key, node);
            // 成功添加node.
            return new PutVO<>(null, null, "ADD_NODE");
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 参考frameTable的状态移除key
     * @return 返回对应value
     */
    @Override
    public V remove(K key, HashMap<Integer, FrameDescriptor> frameTable) {
        if (frameTable.containsKey(key)) {
            if (frameTable.get(key).isPinned() || frameTable.get(key).getPinCount().intValue() > 0) {
                return null;
            }
            // 该页没有被固定或者没有被访问.
            V value = map.get(key).value;
            removeNode(map.get(key));
            map.remove(key);
            // 同时在frameTable中删除该页.
            frameTable.remove(key);
            return value;
        }
        return null;
    }

    @Override
    public Boolean contains(K key) {
        return map.containsKey(key);
    }

    @Override
    public V getWithoutMove(K key) {
        if (map.get(key) == null) {
            return null;
        }
        return map.get(key).value;
    }

    @Override
    public Integer getMaxMemorySize() {
        return this.maxCapacity;
    }

    @Override
    public Integer getMemorySize() {
        readLock.lock();
        try {
            return map.size();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void showReplacerStatus() {
        showLRUList();
    }

    @Override
    public void clear() {

    }

    @Override
    public Integer getHitCounts() {
        return ioHitCount.intValue();
    }

    public void showLRUList() {
        Node<K, V> cur = head;
//        ArrayList<String> result = new ArrayList<>();
        while (cur != null) {
//            result.add(cur.toString());
            System.out.println(cur);
            cur = cur.next;
        }
//        System.out.println(result);
    }
}
