package phanastrae.arachne.render;

import java.util.ArrayList;
import java.util.List;

public class BufferHolders {

    static long lastRelease = Long.MIN_VALUE;
    static List<BufferHolder> trackedHolders = new ArrayList<>();

    public static void storeBufferHolder(BufferHolder holder) {
        // start tracking this buffer holder
        trackedHolders.add(holder);
    }

    public static void releaseUnused() {
        // release all tracked buffers that have not been used recently
        List<BufferHolder> released = new ArrayList<>();
        for(BufferHolder bh : trackedHolders) {
            if(bh.timeFromAccess() > 5E9) { // if unused for 5 seconds
                bh.release();
                released.add(bh);
            }
        }
        trackedHolders.removeAll(released);
        lastRelease = System.nanoTime();
    }

    public static void releaseAll() {
        // release all tracked buffers
        for(BufferHolder bh : trackedHolders) {
            bh.release();
        }
        trackedHolders.clear();
        lastRelease = System.nanoTime();
    }

    public static long timeFromRelease() {
        long dt = System.nanoTime() - lastRelease;
        if(dt < 0) dt = -dt;
        return dt;
    }
}
