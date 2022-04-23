package replacer;

public class Node<K, V> {
    public K key;
    public V value;
    public Node<K, V> pre, next;

    public Node() {}
    public Node(K key, V value) {
        this.key = key;
        this.value = value;
    }

//    public Node<K, V> getPre() {
//        return pre;
//    }
//
//    public void setPre(Node<K, V> pre) {
//        this.pre = pre;
//    }
//
//    public Node<K, V> getNext() {
//        return next;
//    }
//
//    public void setNext(Node<K, V> next) {
//        this.next = next;
//    }
//
//    public K getKey() {
//        return this.key;
//    }
//
//    public V getValue() {
//        return this.value;
//    }
//
//    public void setKey(K key) {
//        this.key = key;
//    }
//
//    public void setValue(V value) {
//        this.value = value;
//    }

    @Override
    public String toString() {
        return "key:" + key.toString() + " value:" + value.toString();
    }
}
