package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */

public class LandMark {
    private final String id;
    private final String description;
    private final List<CloudPoint> coordinates;

    public LandMark(String id, String description, List<CloudPoint> coordinates) {
        this.id = id;
        this.description = description;
        this.coordinates = coordinates;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public List<CloudPoint> getCoordinates() {
        return coordinates;
    }

    public void setAverageCoordinates(List<CloudPoint> newcoordinatesList) {
        if (coordinates.size() <= newcoordinatesList.size()) {
            for (int i = 0; i < coordinates.size(); i++) {
                this.coordinates.set(i,new CloudPoint((this.coordinates.get(i).getX()+newcoordinatesList.get(i).getX())/2,(this.coordinates.get(i).getY()+newcoordinatesList.get(i).getY())/2));
            }
        }else {
            for (int i = 0; i < newcoordinatesList.size(); i++) {
                this.coordinates.set(i,new CloudPoint((this.coordinates.get(i).getX()+newcoordinatesList.get(i).getX())/2,(this.coordinates.get(i).getY()+newcoordinatesList.get(i).getY())/2));
            }
        }
    }

    @Override
    public String toString() {
        return "Landmark{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", coordinates=" + coordinates +
                '}';
    }
}
