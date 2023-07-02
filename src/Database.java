import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;

/**
 * This class represents a database from and to which threads can read and write
 */

public class Database {
    private Map<String, String> data;
    private int numOfReaders; //number of threads currently reading from database
    private boolean isSomeoneWriting;
    private Set<Long> pidReaders; //set of id's of all threads currently reading from database
    private Long pidWriter; //the id of the thread currently writing into database, null if there isn't one
    private final int MAX_READERS; //maximum number of threads that cn read from database at the same time
    private Lock accessTInfoLock; //lock for accessing the information regarding threads using the database

    // condition of the accessTInfoLock - manages the waiting of threads until they can write or read
    private Condition canReadWrite;

    /**
     * Constructor of database
     * @param maxNumOfReaders maximum number of threads that can read from database at the same time
     */
    public Database(int maxNumOfReaders) {
        data = new HashMap<>();
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

    /**
     * The thread tries to acquire read access, if it can imminently it does, otherwise
     * it does nothing and the function returns false
     * @return true if succeeded, false otherwise
     */
    public boolean readTryAcquire() {
        this.accessTInfoLock.lock();
        //checking can read - if there are less than maximum readers and no thread is writing
        boolean canRead = (this.numOfReaders < this.MAX_READERS) && (!(this.isSomeoneWriting));
        if(canRead){
            //updating thread info as reader in the attributes
            this.numOfReaders++;
            this.pidReaders.add(Thread.currentThread().getId());
        }
        this.accessTInfoLock.unlock();
        return canRead;
    }

    /**
     * Acquiring reading access to the database -
     * The thread waits till it can have access to the database and only then acquires read access
     */
    public void readAcquire(){
        try {
            this.accessTInfoLock.lock();
            //checking can read - if there are less than maximum readers and no thread is writing
            while ((this.numOfReaders >= this.MAX_READERS) || (this.isSomeoneWriting)) {
                this.canReadWrite.await();
            }
            //updating thread info as reader in the attributes
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

    /**
     * Releasing read access to the database
     * @throws IllegalMonitorStateException - if the process has not acquired read access we won't
     * release access and instead throw exception with string "Illegal read release attempt"
     */
    public void readRelease() {
        this.accessTInfoLock.lock();
        //checking if the thread acquired read access
        if(!this.pidReaders.contains(Thread.currentThread().getId())) {
            this.accessTInfoLock.unlock();
            throw new IllegalMonitorStateException("Illegal read release attempt");
        }
        //erasing thread info as reader in the attributes
        this.pidReaders.remove(Thread.currentThread().getId());
        this.numOfReaders--;
        this.canReadWrite.signal();
        this.accessTInfoLock.unlock();
    }

    /**
     * The thread tries to acquire write access, if it can imminently it does, otherwise
     * it does nothing and the function returns false
     * @return true if succeeded, false otherwise
     */
    public boolean writeTryAcquire() {
        this.accessTInfoLock.lock();
        // checking can write - only if no other thread is reading or writing
        boolean canWrite = (this.numOfReaders == 0) && (!(this.isSomeoneWriting));
        if(canWrite) {
            this.isSomeoneWriting = true;
            this.pidWriter = Thread.currentThread().getId();
        }
        //updating thread info as writer in the attributes
        this.accessTInfoLock.unlock();
        return canWrite;
    }

    /**
     * Acquiring writing access to the database -
     * The thread waits till it can have access to the database and only then acquires write access
     */
    public void writeAcquire() {
        // TODO: deal with busy waiting
        try{
            this.accessTInfoLock.lock();
            // checking can write - only if no other thread is reading or writing
            while ((this.numOfReaders > 0) || (this.isSomeoneWriting)) {
                this.canReadWrite.await();
            }
            //updating thread info as writer in the attributes
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

    /**
     * Releasing write access to the database
     * @throws IllegalMonitorStateException - if the process has not acquired write access we won't
     * release access and instead throw exception with string "Illegal write release attempt"
     */
    public void writeRelease() {
        this.accessTInfoLock.lock();
        if(this.pidWriter == null || this.pidWriter != Thread.currentThread().getId()){
            this.accessTInfoLock.unlock();
            throw new IllegalMonitorStateException("Illegal write release attempt");
        }
        //erasing thread info as writer in the attributes
        this.pidWriter = null;
        this.isSomeoneWriting = false;
        this.canReadWrite.signal();
        this.accessTInfoLock.unlock();
    }
}