package icu.shishc.replacer;

import icu.shishc.interfac3.Replacer;
import icu.shishc.interfac3.PutVO;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SingleListLRUReplacer<K, V> implements Replacer<K, V> {

//    private final Integer DEFAULT_CAPACITY = 16;
    private final Integer maxCapacity;

    private AtomicInteger curSize = new AtomicInteger(0);
    private Node<K, V> head;

    private AtomicInteger ioCount;
    private AtomicInteger ioHitCount = new AtomicInteger(0);

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public SingleListLRUReplacer(Integer capacity) {
        maxCapacity = capacity;
    }

    public AtomicInteger getIoHitCount() {
        return ioHitCount;
    }

    public void setIoHitCount(AtomicInteger ioHitCount) {
        this.ioHitCount = ioHitCount;
    }

    private void addNode(Node<K, V> node) {
        if (node == null) {
            return;
        }
        if (head == null) {
            head = node;
        } else {
            Node<K, V> cur = head;
            while (cur.next != null) {
                cur = cur.next;
            }
            cur.next = node;
        }
    }

    private void removeNode(Node<K, V> node) {
        if (node == null) {
            return;
        }

        // 如果要删除节点是头节点，移除头节点并清空next.
        if (head.key.equals(node.key) && head.value.equals(node.value)) {
            Node<K, V> cur = head;
            head = head.next;
            cur.next = null;
            return;
        }

        Node<K, V> cur = head;
        while (!cur.next.key.equals(node.key) && !cur.next.value.equals(node.value)) {
            cur = cur.next;
        }
        cur.next = node.next;
        node.next = null;
    }

    /**
     * 获取一个K, V
     */
    @Override
    public V get(K key) {
        writeLock.lock();
        try {
            V returnValue = null;
            Node<K, V> cur = head;
            while (cur != null && cur.key != key) {
                cur = cur.next;
            }
            if (cur != null) {
//                ioHitCount.incrementAndGet();
                returnValue = cur.value;
            }
            // 找到当前节点进行移除并添加，即将其移到链尾.
            removeNode(cur);
            addNode(cur);
            return returnValue;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public PutVO put(K key, V value, HashMap<Integer, FrameDescriptor> frameTable) {
        writeLock.lock();
        try {
            Node<K, V> node = getNodeWithoutMove(key);
            Node<K, V> newNode = new Node<>(key, value);
            if (node == null) {
                if (getMemorySize() >= maxCapacity) {
                    Node<K, V> curNode = head;
                    while (curNode != null && frameTable.get(curNode.key).isPinned() || frameTable.get(curNode.key).getPinCount().intValue() > 0) {
                        curNode = curNode.next;
                    }
                    removeNode(curNode);
                    addNode(newNode);
                    return new PutVO<>(curNode.key, curNode.value, null);
                } else {
                    addNode(newNode);
                    curSize.incrementAndGet();
                    return new PutVO<>(null, null, "ADD_NODE");
                }
            } else {
                ioHitCount.incrementAndGet();
                removeNode(node);
                addNode(newNode);
                return new PutVO<>(null, null, "KEY_IN_POOL");
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public V remove(K key, HashMap<Integer, FrameDescriptor> frameTable) {
        writeLock.lock();
        try {
            Node<K, V> curNode = head;
            while (curNode.key != key) {
                curNode = curNode.next;
            }
            if (!frameTable.get(key).isPinned() && frameTable.get(key).getPinCount().intValue() == 0) {
                removeNode(curNode);
                curSize.decrementAndGet();
                return curNode.value;
            }
            return null;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Boolean contains(K key) {
        V value = getWithoutMove(key);
        return value != null;
    }

    /**
     * 通过key获取单链表中节点的method.
     */
    private Node<K, V> getNodeWithoutMove(K key) {
        Node<K, V> cur = head;
        while (cur != null && cur.key != key) {
            cur = cur.next;
        }
        return cur;
    }

    @Override
    public V getWithoutMove(K key) {
        Node<K, V> cur = head;
        while (cur != null && cur.key != key) {
            cur = cur.next;
        }
        if (cur != null) {
            return cur.value;
        }
        return null;
    }

    @Override
    public Integer getMaxMemorySize() {
        return maxCapacity;
    }

    @Override
    public Integer getMemorySize() {
        return curSize.intValue();
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
            System.out.println(cur);
//            result.add(cur.toString());
            cur = cur.next;
        }
//        System.out.println(result);
    }
}
