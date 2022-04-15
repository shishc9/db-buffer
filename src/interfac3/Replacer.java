package interfac3;

public interface Replacer<K, V> {

    V get(K key);

    V put(K key, V value);

    Integer getMaxMemorySize();

    Integer getMemorySize();

    void clear();
}
