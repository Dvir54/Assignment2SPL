package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.DetectObjectsEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    private ArrayList<DetectObjectsEvent> doneDetectObjectsEvents;
    String databasePath;
    LiDarDataBase liDarDataBase;


    public enum Status {
        UP, DOWN, ERROR
    }

    public LiDarWorkerTracker(int id, int frequency, String databasePath) {
        this.id = id;
        this.frequency = frequency;
        this.status = Status.UP;
        this.lastTrackedObjects = new ArrayList<>();
        this.databasePath = databasePath;
        this.liDarDataBase = LiDarDataBase.getInstance(databasePath);
        this.doneDetectObjectsEvents = new ArrayList<>();
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


    public boolean checkIfError(int time){
        for(StampedCloudPoints stampedCloudPoints : liDarDataBase.getListCloudPoints()){
            if(stampedCloudPoints.getTime() == time){
                if(stampedCloudPoints.getId().equals("ERROR")){
                    return true;
                }
            }
        }
        return false;
    }

    public ArrayList<TrackedObject> getSendedTrackedObjects(ConcurrentLinkedQueue<DetectObjectsEvent> detectObjectsEvents, int time){
        ArrayList<TrackedObject> sendTrackedObjects = new ArrayList<>();
        for(DetectObjectsEvent detectObjectsEvent : detectObjectsEvents){
            if(detectObjectsEvent.getStampedDetectedObjects().getTime() + getFrequency() <= time){
                for (DetectedObject detectedObject : detectObjectsEvent.getStampedDetectedObjects().getDetectedObjectsList()){
                    String id = detectedObject.getId();
                    if(liDarDataBase.getCloudPoint(id, detectObjectsEvent.getStampedDetectedObjects().getTime()) != null){
                        StampedCloudPoints stampedCloudPoints = liDarDataBase.getCloudPoint(id, detectObjectsEvent.getStampedDetectedObjects().getTime());
                        TrackedObject trackedObject = new TrackedObject(id, detectObjectsEvent.getStampedDetectedObjects().getTime() + getFrequency(), detectedObject.getDescription(), stampedCloudPoints.toCloudPointList());
                        sendTrackedObjects.add(trackedObject);
                    }
                }
                detectObjectsEvents.remove(detectObjectsEvent);
                doneDetectObjectsEvents.add(detectObjectsEvent);
            }
        }
        return sendTrackedObjects;
    }

    public void addTrackedObject(TrackedObject trackedObject) {
        lastTrackedObjects.add(trackedObject);
    }

    public void setLastTrackedObjects(ArrayList<TrackedObject> trackedObjects){
        lastTrackedObjects.clear();
        lastTrackedObjects.addAll(trackedObjects);

    }

    public ArrayList<DetectObjectsEvent> getDoneDetectObjectsEvents() {
        return doneDetectObjectsEvents;
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
