package replacer;

import interfac3.Replacer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LRUReplacer<K, V> implements Replacer<K, V> {

//    private final Integer DEFAULT_CAPACITY = 16;

    private final Integer maxCapacity;
    private Map<K, Node<K, V>> map;
    private Node<K, V> head, tail;

    private AtomicInteger ioCount;
    private AtomicInteger ioHitCount;

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
            return node.value;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        writeLock.lock();
        try {
            if (map.containsKey(key)) {
                Node<K, V> node = map.get(key);
                node.value = value;
                removeNode(node);
                addNode(node);
                return node.value;
            } else {
                if (getMemorySize() >= maxCapacity) {
                    map.remove(head.key);
                    removeNode(head);
                }
                Node<K, V> node = new Node<>(key, value);
                addNode(node);
                map.put(key, node);
                return node.value;
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Boolean contains(K key) {
        return map.containsKey(key);
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
    public void clear() {

    }

    public void showLRUList() {
        Node<K, V> cur = head;
        ArrayList<String> result = new ArrayList<>();
        while (cur != null) {
            result.add(cur.toString());
            cur = cur.next;
        }
        System.out.println(result);
    }
}
