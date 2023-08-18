package phanastrae.arachne.thread;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RunnableQueue {

    boolean shouldClose = false;

    List<Runnable> queue = new ArrayList<>();

    Lock lock = new ReentrantLock();

    final Object syncObject = new Object();

    Thread[] threads;

    public RunnableQueue(int threadCount) {
        this.threads = new Thread[threadCount];
        for(int i = 0; i < threadCount; i++) {
            this.threads[i] = new Thread(null, this::threadAction, "arachne_queue_"+i, 0);
            this.threads[i].start();
        }
    }

    public void lock() {
        this.lock.lock();
    }

    public void unlock() {
        this.lock.unlock();
    }

    public void queue(Runnable runnable) {
        this.lock();
        try {
            queue.add(runnable);
            synchronized (syncObject) {
                syncObject.notifyAll();
            }
        } finally {
            this.unlock();
        }
    }

    public Runnable getNext() {
        Runnable r = null;
        this.lock();
        try {
            if(!queue.isEmpty()) {
                r = queue.get(0);
                queue.remove(0);
            }
        } finally {
            this.unlock();
        }
        return r;
    }

    public void close() {
        this.lock();
        try {
            this.shouldClose = true;
            for(Thread thread : this.threads) {
                thread.interrupt();
            }
        } finally {
            this.unlock();
        }
    }

    public void threadAction() {
        while (!this.shouldClose) {
            try {
                Runnable r = this.getNext();
                while(r == null) {
                    synchronized (syncObject) {
                        syncObject.wait();
                    }
                    r = this.getNext();
                }
                r.run();
            } catch (InterruptedException ignored) {
            }
        }
    }
}
