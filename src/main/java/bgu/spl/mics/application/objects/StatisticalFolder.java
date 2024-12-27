package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */

public class StatisticalFolder {
    private final AtomicInteger systemRuntime;
    private final AtomicInteger numDetectedObjects;
    private final AtomicInteger numTrackedObjects;
    private final AtomicInteger numLandmarks;

    public StatisticalFolder() {
        this.systemRuntime = new AtomicInteger(0);
        this.numDetectedObjects = new AtomicInteger(0);
        this.numTrackedObjects = new AtomicInteger(0);
        this.numLandmarks = new AtomicInteger(0);
    }

    private static class StatisticalFolderHolder {
        private static final StatisticalFolder INSTANCE = new StatisticalFolder();
    }

    public static StatisticalFolder getInstance() {
        return StatisticalFolderHolder.INSTANCE;
    }

    public int getSystemRuntime() {
        return systemRuntime.get();
    }

    public void incrementSystemRuntime(int ticks) {
        systemRuntime.addAndGet(ticks);
    }

    public int getNumDetectedObjects() {
        return numDetectedObjects.get();
    }

    public void incrementDetectedObjects(int count) {
        numDetectedObjects.addAndGet(count);
    }

    public int getNumTrackedObjects() {
        return numTrackedObjects.get();
    }

    public void incrementTrackedObjects(int count) {
        numTrackedObjects.addAndGet(count);
    }

    public int getNumLandmarks() {
        return numLandmarks.get();
    }

    public void incrementLandmarks(int count) {
        numLandmarks.addAndGet(count);
    }

    @Override
    public String toString() {
        return "StatisticalFolder {" +
                "\n  System Runtime: " + systemRuntime.get() + " ticks," +
                "\n  Detected Objects: " + numDetectedObjects.get() + "," +
                "\n  Tracked Objects: " + numTrackedObjects.get() + "," +
                "\n  Landmarks: " + numLandmarks.get() + "\n}";
    }
}
