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
    LiDarDataBase liDarDataBase;


    public enum Status {
        UP, DOWN, ERROR
    }

    public LiDarWorkerTracker(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        this.status = Status.UP;
        this.lastTrackedObjects = new ArrayList<>();
        this.liDarDataBase = LiDarDataBase.getInstance("src/main/resources/LiDarData.json");
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

    public ArrayList<TrackedObject> createTrackedObjectsList(List<DetectedObject> detectedObjectList, int time){
        ArrayList<TrackedObject> trackedObjects = new ArrayList<>();
        for (DetectedObject detectedObject : detectedObjectList){
            String id = detectedObject.getId();
            StampedCloudPoints stampedCloudPoints = liDarDataBase.getCloudPoint(id, time);
            TrackedObject trackedObject = new TrackedObject(id, time + getFrequency(), detectedObject.getDescription(), stampedCloudPoints.toCloudPointList());
            trackedObjects.add(trackedObject);
        }
        return trackedObjects;
    }

    public ArrayList<TrackedObject> createListToSend(int time, ArrayList<TrackedObject> trackedObjects){
        ArrayList<TrackedObject> sendTrackedObjects = new ArrayList<>();
        for(TrackedObject trackedObject : trackedObjects){
            if(trackedObject.getTime() <= time){
                if(liDarDataBase.getCloudPoint(trackedObject.getId(), time).getId().equals("ERROR")){
                    setStatus(Status.ERROR);
                    return null;
                }
                else{
                    sendTrackedObjects.add(trackedObject);
                    trackedObjects.remove(trackedObject);
                    lastTrackedObjects.add(trackedObject);
                }
            }
        }
        return sendTrackedObjects;
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
