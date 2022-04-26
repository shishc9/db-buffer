package replacer;

import interfac3.Replacer;

import java.util.ArrayList;
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
    private AtomicInteger ioHitCount;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public SingleListLRUReplacer(Integer capacity) {
        maxCapacity = capacity;
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

    private Boolean removeNode(Node<K, V> node) {
        if (node == null) {
            return true;
        }

        if (head.key.equals(node.key) && head.value.equals(node.value)) {
            head = head.next;
            return true;
        }

        Node<K, V> cur = head;
        while (!cur.next.key.equals(node.key) && !cur.next.value.equals(node.value)) {
            cur = cur.next;
        }
        cur.next = node.next;
        node.next = null;
        return true;
    }

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
                returnValue = cur.value;
            }
            removeNode(cur);
            addNode(cur);
            return returnValue;
        } finally {
            writeLock.unlock();
        }
    }

    //TODO: V.type = Node; 实际添加一个V的时候要同步Node中的key，实际key就是PageID.
    //后续考虑要不要修改 K V类型.
    @Override
    public V put(K key, V value, HashMap<Integer, FrameDescriptor> frameTable) {
        writeLock.lock();
        try {
            V v = getWithoutMove(key);
            Node<K, V> node = new Node<>(key, value);
            if (v == null) {
                if (getMemorySize() >= maxCapacity) {
                    Node<K, V> curNode = head;
                    while (frameTable.get(curNode.key).isPinned() || frameTable.get(curNode.key).getPinCount().intValue() > 0) {
                        curNode = curNode.next;
                    }
                    removeNode(curNode);
                    addNode(node);
                } else {
                    addNode(node);
                    curSize.incrementAndGet();
                    return value;
                }
            } else {
                removeNode(node);
                addNode(node);
                return value;
            }
        } finally {
            writeLock.unlock();
        }
        return null;
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
