package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.TrackedObjectsEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */

public class FusionSlam {
    private final List<LandMark> landmarks;
    private final List<Pose> poses;
    private int countMicroServices;
    private StatisticalFolder statisticalFolder;

    // Private constructor for Singleton
    private FusionSlam() {
        this.landmarks = new ArrayList<>();
        this.poses = new ArrayList<>();
        this.countMicroServices = 0;
        this.statisticalFolder = StatisticalFolder.getInstance();
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
            statisticalFolder.incrementLandmarks(1);
            this.addLandmark(new LandMark(newLandMark.getId(), newLandMark.getDescription(), newLandMark.getCoordinates()));
        }
    }

    public void createLandMarks(TrackedObjectsEvent trackedObjectsEvent, int time){
            List<TrackedObject> trackedObjects = trackedObjectsEvent.getTrackedObjectsList();
            Pose pose = poses.get(time - 2);
            for (TrackedObject trackedObject : trackedObjects) {
                List<CloudPoint> list = trackedObject.calculateGlobalCoordinates(pose.getX(), pose.getY(), pose.getYaw());
                LandMark newLandMark = new LandMark(trackedObject.getId(), trackedObject.getDescription(), list);
                updateLandmark(newLandMark);
            }

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
