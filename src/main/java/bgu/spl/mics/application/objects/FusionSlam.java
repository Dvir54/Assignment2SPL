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

    // Private constructor for Singleton
    private FusionSlam() {
        this.landmarks = new ArrayList<>();
        this.poses = new ArrayList<>();
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

    public void updateLandmark(TrackedObject obj) {
        Boolean isExist = false;
        Pose pose = poses.get(obj.getTime());
        List<CloudPoint> list = obj.calculateGlobalCoordinates(pose.getX(), pose.getY(), pose.getYaw());

        for (LandMark landmark : landmarks) {
            if (landmark.getId().equals(obj.getId())){

                if (landmark.getCoordinates().size() >= obj.getCoordinates().size()) {
                    landmark.setAverageCoordinates(list);

                }else {
                    List<CloudPoint> prevList = landmark.getCoordinates();
                    landmark = new LandMark(obj.getId(), obj.getDescription(), list);
                    landmark.setAverageCoordinates(prevList);
                }
                isExist = true;
            }
        }
        if (!isExist) {
            this.addLandmark(new LandMark(obj.getId(), obj.getDescription(), list));
        }
    }


    @Override
    public String toString() {
        return "FusionSlam{" +
                "landmarks=" + landmarks +
                ", poses=" + poses +
                '}';
    }
}
