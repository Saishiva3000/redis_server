import java.lang.reflect.Array;

public class IncrementalHashMap <K,V>{

    private Table<K,V> newTable = null;
    private Table<K,V> oldTable = new Table<>(8);
    private static final int MAX_LOAD_FACTOR = 8;
    private boolean REHASHING_TRIGGERED = false;
    private int noOfEntries = 0;
    private int migratePosition = 0;
    private int REHASHING_WORK = 128;

    public Integer put(K key,V value){
        int triggerFactor = (int) (MAX_LOAD_FACTOR*oldTable.getSize());
        if(noOfEntries > triggerFactor){
            triggerRehashing();
        }
        if(REHASHING_TRIGGERED){
            int index = (key.hashCode() & newTable.getMash());
            Bucket<K,V> bucket = newTable.getBucket(index);
            if(bucket == null){
                bucket = new Bucket<>(key,value);
                newTable.setBucket(index,bucket);
                helpRehashing();
                noOfEntries++;
                return 1;
            }
            bucket.addEntry(key,value);
            helpRehashing();
        }
        else {
            int index = key.hashCode() & oldTable.getMash();
            Bucket<K,V> bucket = oldTable.getBucket(index);
            if(bucket == null){
                bucket = new Bucket<>(key,value);
                oldTable.setBucket(index,bucket);
                noOfEntries++;
                return 1;
            }
            bucket.addEntry(key,value);
        }
        noOfEntries++;
        return 1;
    }

    public V get(K key){
        KeyValueStore<K,V> keyValueStore = null;
        if(REHASHING_TRIGGERED){
            int index = key.hashCode() & newTable.getMash();
            Bucket<K,V> newTableBucket = newTable.getBucket(index);
            if(newTableBucket != null){
                keyValueStore = newTableBucket.getEntry(key);
            }
            if(keyValueStore == null){
                index = key.hashCode() & oldTable.getMash();
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
            int index = key.hashCode() & oldTable.getMash();
            Bucket<K,V> bucket = oldTable.getBucket(index);
            if(bucket == null){
                return null;
            }
            keyValueStore = bucket.getEntry(key);
        }
        return keyValueStore == null ? null : keyValueStore.getValue();
    }

    public Integer remove(K key){
        if(REHASHING_TRIGGERED){
            int index = key.hashCode() & newTable.getMash();
            Bucket<K,V> newTableBucket = newTable.getBucket(index);
            if(newTableBucket == null){
                return 0;
            }
            KeyValueStore<K,V> keyValueStore = newTableBucket.delEntry(key);
            if(keyValueStore == null){
                index = key.hashCode() & oldTable.getMash();
                Bucket<K,V> oldTableBucket = oldTable.getBucket(index);
                if(oldTableBucket == null){
                    return 0;
                }
                oldTableBucket.delEntry(key);
            }
            helpRehashing();
        }
        else {
            int index = key.hashCode() & oldTable.getMash();
            Bucket<K,V> oldTableBucket = oldTable.getBucket(index);
            if(oldTableBucket == null){
                return 0;
            }
            oldTableBucket.delEntry(key);
        }
        return 1;
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
            Bucket<K,V> bucket = oldTable.getBucket(migratePosition);
            if(bucket != null){
                KeyValueStore<K,V> head = bucket.getHead();
                while(head != null){
                    int index = head.getKey().hashCode() & newTable.getMash();
                    Bucket<K,V> newBucket = newTable.getBucket(index);
                    if(newBucket == null){
                        newBucket = new Bucket<>(head.getKey(),head.getValue());
                        newTable.setBucket(index,bucket);
                        head = head.getNext();
                        continue;
                    }
                    newBucket.addEntry(head.getKey(), head.getValue());
                    head = head.getNext();
                }
            }
            migratePosition++;
            if(migratePosition >= oldTable.getSize()){
                REHASHING_TRIGGERED = false;
                oldTable = newTable;
                newTable = null;
                return;
            }
        }
    }
}
