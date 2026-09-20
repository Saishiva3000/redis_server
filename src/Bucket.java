import java.security.Key;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Bucket<K,V> {

    private KeyValueStore<K,V> head;

    public Bucket(K key, V value) {
        this.head = new KeyValueStore<>(key,value);
        head.setNext(null);
    }

    public Bucket(KeyValueStore<K,V> head){
        this.head = head;
    }

    public void addEntry(K key, V value){
        KeyValueStore<K,V> existing = this.getEntry(key);
        if(existing != null){
            existing.setValue(value);
            return;
        }
        KeyValueStore<K,V> keyValueStore = new KeyValueStore<>(key,value);
        keyValueStore.setNext(head);
        head = keyValueStore;
    }

    public KeyValueStore<K,V> getEntry(K key){
        KeyValueStore<K,V> temp = head;
        while (temp != null){
            if(temp.getKey().equals(key)){
                return temp;
            }
            temp = temp.getNext();
        }
        return null;
    }

    public KeyValueStore<K,V> delEntry(K key){
        KeyValueStore<K,V> temp = head;
        if(temp != null && temp.getKey().equals(key)){
            head = temp.getNext();
        }
        while (temp != null && temp.getNext() != null){
            if(temp.getNext().getKey().equals(key)){
                KeyValueStore<K,V> returnValue = temp.getNext();
                temp.setNext(temp.getNext().getNext());
                return returnValue;
            }
            temp = temp.getNext();
        }
        return temp;
    }


    public KeyValueStore<K, V> getHead() {
        return head;
    }
}
