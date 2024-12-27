package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */

public class Camera {

    private final int id;
    private final int frequency;
    private Status status;
    private final List<StampedDetectedObjects> detectedObjectsList;

    public enum Status {
        UP, DOWN, ERROR
    }

    public Camera(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        this.status = Status.UP;
        this.detectedObjectsList = new ArrayList<>();
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

    public List<StampedDetectedObjects> getDetectedObjectsList() {
        return detectedObjectsList;
    }

    public void addStampedDetectedObject(StampedDetectedObjects detectedObject) {
        detectedObject.setTime(this.frequency);
        detectedObjectsList.add(detectedObject);
    }

    @Override
    public String toString() {
        return "Camera{" +
                "id=" + id +
                ", frequency=" + frequency +
                ", status=" + status +
                ", detectedObjectsList=" + detectedObjectsList +
                '}';
    }
}
