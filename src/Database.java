import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;


public class Database {
    private Map<String, String> data;
    private int numOfReaders;
    private Set<Long> pidReaders;
    private Long pidWriter;
    private boolean isSomeoneWriting;
    private final int MAX_READERS;
    private Lock accessTInfoLock;
    private Condition canReadWrite;

    public Database(int maxNumOfReaders) {
        data = new HashMap<>();  // Note: You may add fields to the class and initialize them in here. Do not add parameters!
        this.MAX_READERS = maxNumOfReaders;
        this.numOfReaders = 0;
        this.isSomeoneWriting = false;
        this.accessTInfoLock = new ReentrantLock();
        this.canReadWrite = this.accessTInfoLock.newCondition();
        this.pidWriter = null;
        this.pidReaders =  new HashSet<Long>();
    }

    public void put(String key, String value) {
        data.put(key, value);
    }

    public String get(String key) {
        return data.get(key);
    }

    public boolean readTryAcquire() {
        this.accessTInfoLock.lock();
        boolean canRead = (this.numOfReaders < this.MAX_READERS) && (!(this.isSomeoneWriting));
        if(canRead){
            this.numOfReaders++;
            this.pidReaders.add(Thread.currentThread().getId());
        }
        this.accessTInfoLock.unlock();
        return canRead;
    }

    public void readAcquire(){
        try {
            this.accessTInfoLock.lock();
            while ((this.numOfReaders >= this.MAX_READERS) || (this.isSomeoneWriting)) {
                this.canReadWrite.await();
            }
            this.numOfReaders++;
            this.pidReaders.add(Thread.currentThread().getId());
        }
        catch (InterruptedException e){
            //Todo : erase this later
            throw new RuntimeException("Interrupted exception in read acquire");
        }
        finally {
            this.accessTInfoLock.unlock();
        }
    }

    public void readRelease() {
        this.accessTInfoLock.lock();
        if(!this.pidReaders.contains(Thread.currentThread().getId())) {
            this.accessTInfoLock.unlock();
            throw new IllegalMonitorStateException("Illegal read release attempt");
        }
        this.pidReaders.remove(Thread.currentThread().getId());
        this.numOfReaders--;
        this.canReadWrite.signal();
        this.accessTInfoLock.unlock();
    }

    public boolean writeTryAcquire() {
        this.accessTInfoLock.lock();
        boolean canWrite = (this.numOfReaders == 0) && (!(this.isSomeoneWriting));
        if(canWrite) {
            this.isSomeoneWriting = true;
            this.pidWriter = Thread.currentThread().getId();
        }
        this.accessTInfoLock.unlock();
        return canWrite;
    }

    public void writeAcquire() {
        // TODO: deal with busy waiting
        try{
            this.accessTInfoLock.lock();
            while ((this.numOfReaders > 0) || (this.isSomeoneWriting)) {
                this.canReadWrite.await();
            }
            this.pidWriter = Thread.currentThread().getId();
            this.isSomeoneWriting = true;
        }
        catch (InterruptedException e){
            //Todo : erase this later
            throw new RuntimeException("Interrupted exception in write acquire");
        }
        finally {
            this.accessTInfoLock.unlock();
        }
    }

    public void writeRelease() {
        this.accessTInfoLock.lock();
        if(this.pidWriter == null || this.pidWriter != Thread.currentThread().getId()){
            this.accessTInfoLock.unlock();
            throw new IllegalMonitorStateException("Illegal write release attempt");
        }
        this.pidWriter = null;
        this.isSomeoneWriting = false;
        this.canReadWrite.signal();
        this.accessTInfoLock.unlock();
    }
}