package phanastrae.arachne.render;

public abstract class BufferHolder<T> {

    protected long lastAccess;

    public abstract T getBuffer();
    public abstract void release();

    public long timeFromAccess() {
        // returns the (absolute) time since last access, in nanoseconds
        long dt = System.nanoTime() - lastAccess;
        if(dt < 0) dt = -dt;
        return dt;
    }
}
