package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description, 
 * time of tracking, and coordinates in the environment.
 */

public class TrackedObject {
    private final String id;
    private final int time;
    private final String description;
    private final List<CloudPoint> coordinates;

    public TrackedObject(String id, int time, String description, List<CloudPoint> coordinates) {
        this.id = id;
        this.time = time;
        this.description = description;
        this.coordinates = coordinates;
    }

    public String getId() {
        return id;
    }

    public int getTime() {
        return time;
    }

    public String getDescription() {
        return description;
    }

    public List<CloudPoint> getCoordinates() {
        return coordinates;
    }

    public List<CloudPoint> calculateGlobalCoordinates(double x, double y, double yaw) {
        List<CloudPoint> globalCoordinates = new ArrayList<>();

        double yawRad = Math.toRadians(yaw);

        for (CloudPoint coordinate : coordinates) {
            double xCoord = coordinate.getX() * Math.cos(yawRad) - coordinate.getY() * Math.sin(yawRad);
            double yCoord = coordinate.getX() * Math.sin(yawRad) + coordinate.getY() * Math.cos(yawRad);
            globalCoordinates.add(new CloudPoint(xCoord, yCoord));
        }

        return globalCoordinates;
    }

    @Override
    public String toString() {
        return "TrackedObject{" +
                "id='" + id + '\'' +
                ", time=" + time +
                ", description='" + description + '\'' +
                ", coordinates=" + coordinates +
                '}';
    }
}
