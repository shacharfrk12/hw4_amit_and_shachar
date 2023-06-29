import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;


public class Database {
    private Map<String, String> data;
    private int numOfReaders;
    private final int MAX_READERS;
    private Lock accessNumReaderLock;

    public Database(int maxNumOfReaders) {
        data = new HashMap<>();  // Note: You may add fields to the class and initialize them in here. Do not add parameters!
        this.MAX_READERS = maxNumOfReaders;
        this.accessNumReaderLock = new ReentrantLock();
    }

    public void put(String key, String value) {
        data.put(key, value);
    }

    public String get(String key) {
        return data.get(key);
    }

    public boolean readTryAcquire() {
        this.accessNumReaderLock.lock();
        boolean canRead = this.numOfReaders <= this.MAX_READERS;
        this.accessNumReaderLock.unlock();
        return canRead;
    }

    public void readAcquire() {
        this.accessNumReaderLock.lock();
        this.numOfReaders++;
        this.accessNumReaderLock.unlock();
        this.get();
        // TODO: Add your code here...
    }

    public void readRelease() {
        // TODO: Add your code here...
    }

    public void writeAcquire() {
       // TODO: Add your code here...
    }

    public boolean writeTryAcquire() {
        // TODO: Add your code here...
    }

    public void writeRelease() {
        // TODO: Add your code here...
    }
}