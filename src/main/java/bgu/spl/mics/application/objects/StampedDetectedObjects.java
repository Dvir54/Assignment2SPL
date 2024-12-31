package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */

public class StampedDetectedObjects {
    private int time;
    private final List<DetectedObject> detectedObjectsList;

    public StampedDetectedObjects(int time, List<DetectedObject> detectedObjects) {
        this.time = time;
        this.detectedObjectsList = detectedObjects;
    }

    public int getTime() {
        return time;
    }

    public List<DetectedObject> getDetectedObjectsList() {
        return detectedObjectsList;
    }

    //check if to delete
    public void setTime(int val){
        this.time = this.time + val;
    }

    public String checkIfError() {
        for (DetectedObject obj : detectedObjectsList) {
            if (obj.getId().equals("ERROR")) {
                return obj.getDescription();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "StampedDetectedObject{" +
                "time=" + time +
                ", detectedObjects=" + detectedObjectsList +
                '}';
    }
}
