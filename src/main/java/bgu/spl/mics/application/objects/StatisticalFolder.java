package bgu.spl.mics.application.objects;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
    private int systemRuntime;
    private int numDetectedObjects;
    private int numTrackedObjects;
    private int numLandmarks;

    public StatisticalFolder() {
        this.systemRuntime = 0;
        this.numDetectedObjects = 0;
        this.numTrackedObjects = 0;
        this.numLandmarks = 0;
    }

    public int getSystemRuntime() {
        return systemRuntime;
    }

    public void incrementSystemRuntime(int ticks) {
        this.systemRuntime += ticks;
    }

    public int getNumDetectedObjects() {
        return numDetectedObjects;
    }

    public void incrementDetectedObjects(int count) {
        this.numDetectedObjects += count;
    }

    public int getNumTrackedObjects() {
        return numTrackedObjects;
    }

    public void incrementTrackedObjects(int count) {
        this.numTrackedObjects += count;
    }

    public int getNumLandmarks() {
        return numLandmarks;
    }

    public void incrementLandmarks(int count) {
        this.numLandmarks += count;
    }

    @Override
    public String toString() {
        return "StatisticalFolder{" +
                "systemRuntime=" + systemRuntime +
                ", numDetectedObjects=" + numDetectedObjects +
                ", numTrackedObjects=" + numTrackedObjects +
                ", numLandmarks=" + numLandmarks +
                '}';
    }
}
