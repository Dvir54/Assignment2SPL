package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;
/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */

public class LiDarWorkerTracker {

    private final int id;
    private final int frequency;
    private Status status;
    private final List<TrackedObject> lastTrackedObjects;


    public enum Status {
        UP, DOWN, ERROR
    }

    public LiDarWorkerTracker(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        this.status = Status.UP;
        this.lastTrackedObjects = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<TrackedObject> getLastTrackedObjects() {
        return lastTrackedObjects;
    }

    public void addTrackedObject(TrackedObject trackedObject) {
        lastTrackedObjects.add(trackedObject);
    }

    @Override
    public String toString() {
        return "LiDarTrackerWorker{" +
                "id=" + id +
                ", frequency=" + frequency +
                ", status=" + status +
                ", lastTrackedObjects=" + lastTrackedObjects +
                '}';
    }
}
