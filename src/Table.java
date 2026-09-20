import java.lang.reflect.Array;

public class Table<K,V>{

    private Bucket<K,V>[] table;
    private int size;
    private int mash;

    @SuppressWarnings("unchecked")
    public Table(int size) {
        if(size<=0 || (size & (size -1)) !=0){
            throw new IllegalArgumentException("size is not power of two");
        }
        this.size = size;
        this.table = (Bucket<K, V>[]) Array.newInstance(Bucket.class,size);
        this.mash = size-1;
    }

    public void setBucket(int index,Bucket<K,V> bucket){
        table[index] = bucket;
    }

    public Bucket<K,V> getBucket(int index){
        return table[index];
    }

    public Bucket<K, V>[] getTable() {
        return table;
    }

    public void setTable(Bucket<K, V>[] table) {
        this.table = table;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getMash() {
        return mash;
    }

    public void setMash(int mash) {
        this.mash = mash;
    }
}
