
public class KeyValueStore<K,V> {

    private K key;
    private V value;
    private KeyValueStore<K,V> next;

    public KeyValueStore(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public void setNext(KeyValueStore<K, V> next) {
        this.next = next;
    }

    public KeyValueStore<K, V> getNext() {
        return next;
    }
}
