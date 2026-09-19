import java.lang.reflect.Array;

public class IncrementalHashMap <K,V>{

    private Table<K,V> newTable = null;
    private Table<K,V> oldTable = new Table<>(8);
    private static final float THRESHOLD = 0.75f;
    private static boolean REHASHING_TRIGGERED = false;
    private static int noOfEntries = 0;
    private static int migratePosition = 0;
    private static int REHASHING_WORK = 128;

    public void put(K key,V value){
        int triggerFactor = (int) (THRESHOLD*oldTable.getSize());
        if(noOfEntries > triggerFactor){
            triggerRehashing();
        }
        if(REHASHING_TRIGGERED){
            int index = key.hashCode()% newTable.getSize();
            Bucket<K,V> bucket = newTable.getBucket(index);
            if(bucket == null){
                bucket = new Bucket<>(key,value);
                newTable.setBucket(index,bucket);
                return;
            }
            bucket.addEntry(key,value);
            helpRehashing();
        }
        else {
            int index = key.hashCode()% oldTable.getSize();
            Bucket<K,V> bucket = oldTable.getBucket(index);
            if(bucket == null){
                bucket = new Bucket<>(key,value);
                oldTable.setBucket(index,bucket);
                return;
            }
            bucket.addEntry(key,value);
        }
        noOfEntries++;
    }

    public V get(K key){
        if(REHASHING_TRIGGERED){
            int index = key.hashCode()% newTable.getSize();
            Bucket<K,V> newTableBucket = newTable.getBucket(index);
            if(newTableBucket == null){
                return null;
            }
            KeyValueStore<K,V> keyValueStore = newTableBucket.getEntry(key);
            if(keyValueStore == null){
                Bucket<K,V> oldTableBucket = oldTable.getBucket(index);
                if(oldTableBucket == null){
                    return null;
                }
                KeyValueStore<K,V> keyValue = oldTableBucket.getEntry(key);
                return keyValue.getValue();
            }
            helpRehashing();
        }
        else{
            int index = key.hashCode()% oldTable.getSize();
            Bucket<K,V> bucket = oldTable.getBucket(index);
            if(bucket == null){
                return null;
            }
            KeyValueStore<K,V> keyValueStore = bucket.getEntry(key);
            return keyValueStore.getValue();
        }
        return null;
    }

    public void remove(K key){
        if(REHASHING_TRIGGERED){
            int index = key.hashCode()% newTable.getSize();
            Bucket<K,V> newTableBucket = newTable.getBucket(index);
            if(newTableBucket == null){
                return;
            }
            KeyValueStore<K,V> keyValueStore = newTableBucket.delEntry(key);
            if(keyValueStore == null){
                Bucket<K,V> oldTableBucket = oldTable.getBucket(index);
                if(oldTableBucket == null){
                    return;
                }
                oldTableBucket.delEntry(key);
            }
            helpRehashing();
        }
        else {
            int index = key.hashCode()% newTable.getSize();
            Bucket<K,V> oldTableBucket = oldTable.getBucket(index);
            if(oldTableBucket == null){
                return;
            }
            oldTableBucket.delEntry(key);
        }
    }

    public void triggerRehashing(){
        REHASHING_TRIGGERED = true;
        newTable = new Table<>(oldTable.getSize() * 2);
        migratePosition =0;
    }

    public void helpRehashing(){
        if(migratePosition >= oldTable.getSize()){
            REHASHING_TRIGGERED = false;
            oldTable = newTable;
            newTable = null;
            return;
        }
        if(newTable == null){
            newTable = new Table<>(oldTable.getSize() * 2);
        }
        for (int i =0; i<REHASHING_WORK ;i++){
            Bucket<K,V> head = oldTable.getBucket(migratePosition);
            if(head != null){
                K key = head.getKey();
                int index = key.hashCode() % newTable.getSize();
                newTable.setBucket(index,head);
                migratePosition++;
            }
            if(migratePosition >= oldTable.getSize()){
                REHASHING_TRIGGERED = false;
                oldTable = newTable;
                newTable = null;
                return;
            }
        }
    }
}
