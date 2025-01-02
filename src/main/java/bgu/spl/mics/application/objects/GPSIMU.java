package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */

public class GPSIMU {
    private int currentTick;
    private Status status;
    private final List<Pose> poseList;

    public enum Status {
        UP, DOWN, ERROR
    }

    public GPSIMU(List<Pose> poseList) {
        this.currentTick = 0;
        this.status = Status.UP;
        this.poseList = poseList;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Pose> getPoseList() {
        return poseList;
    }

    @Override
    public String toString() {
        return "GPSIMU{" +
                "currentTick=" + currentTick +
                ", status=" + status +
                ", poseList=" + poseList +
                '}';
    }
}
