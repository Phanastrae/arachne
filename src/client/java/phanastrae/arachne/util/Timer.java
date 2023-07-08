package phanastrae.arachne.util;

public class Timer {

    boolean timing = false;
    long startTime = 0;
    long elapsedTime = 0;

    boolean NEEDS_START = true;
    long tickStart = 0;
    double elapsedAverage = 0;
    int ticks = 0;

    public void reset() {
        this.timing = false;
        this.startTime = 0;
        this.elapsedTime = 0;
    }

    public void enable() {
        if(!this.timing) {
            this.startTime = System.nanoTime();
            this.timing = true;
        }
    }

    public void disable() {
        if(this.timing) {
            long endTime = System.nanoTime();
            this.elapsedTime += endTime - this.startTime;
            this.timing = false;
        }
    }

    public void tick() {
        this.ticks++;
        long time = System.nanoTime();
        if(this.NEEDS_START) { // on first tick set start time
            this.startTime = time;
            this.NEEDS_START = false;
            return;
        }

        long timeElapsed = time - tickStart;
        // update this many times per second
        double UPDATE_FREQUENCY = 2;
        if(timeElapsed > (1E9 / UPDATE_FREQUENCY)) {
            this.elapsedAverage = this.elapsedTime / (double)this.ticks;
            this.ticks = 0;
            reset();
            this.tickStart = time;
        }
    }

    public double getAverageElapsedTimeNano() {
        return this.elapsedAverage;
    }

    public double getAverageElapsedTimeMicro() {
        return this.elapsedAverage * 1E-3;
    }

    public double getAverageElapsedTimeMilli() {
        return this.elapsedAverage * 1E-6;
    }

    public double getAverageElapsedTimeSeconds() {
        return this.elapsedAverage * 1E-9;
    }
}
