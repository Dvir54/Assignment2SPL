package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */

public class FusionSlam {
    private final List<LandMark> landmarks;
    private final List<Pose> poses;
    private int countNewLandMarks;
    private int countMicroServices;

    // Private constructor for Singleton
    private FusionSlam() {
        this.landmarks = new ArrayList<>();
        this.poses = new ArrayList<>();
        this.countNewLandMarks = 0;
        this.countMicroServices = 0;
    }

    // Singleton instance holder
    private static class FusionSlamHolder {
        private static final FusionSlam INSTANCE = new FusionSlam();
    }

    // Public method to get the Singleton instance
    public static FusionSlam getInstance() {
        return FusionSlamHolder.INSTANCE;
    }

    public List<LandMark> getLandmarks() {
        return landmarks;
    }

    public List<Pose> getPoses() {
        return poses;
    }

    public void addPose(Pose pose) {
        poses.add(pose);
    }

    public void addLandmark(LandMark landmark) {
        landmarks.add(landmark);
    }

    public void updateLandmark(LandMark newLandMark) {
        countNewLandMarks = 0;
        Boolean isExist = false;
        for (int i = 0; i < landmarks.size(); i++) {
            if (landmarks.get(i).getId().equals(newLandMark.getId())){

                if (landmarks.get(i).getCoordinates().size() >= newLandMark.getCoordinates().size()) {
                    landmarks.get(i).setAverageCoordinates(newLandMark.getCoordinates());

                }else {
                    List<CloudPoint> prevList = landmarks.get(i).getCoordinates();
                    landmarks.remove(landmarks.get(i));
                    LandMark landmark = new LandMark(newLandMark.getId(), newLandMark.getDescription(), newLandMark.getCoordinates());
                    landmark.setAverageCoordinates(prevList);
                    landmarks.add(i, landmark);
                }
                isExist = true;
            }

        }
        if (!isExist) {
            countNewLandMarks = countNewLandMarks + 1;
            this.addLandmark(new LandMark(newLandMark.getId(), newLandMark.getDescription(), newLandMark.getCoordinates()));
        }
    }

    public int getCountNewLandMarks() {
        return countNewLandMarks;
    }

    public int getCountMicroServices() {
        return countMicroServices;
    }

    public void addMicroService() {
        countMicroServices++;
    }

    public void reduceMicroService() {
        countMicroServices--;
    }

    @Override
    public String toString() {
        return "FusionSlam{" +
                "landmarks=" + landmarks +
                ", poses=" + poses +
                '}';
    }
}
