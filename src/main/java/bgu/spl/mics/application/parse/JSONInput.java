package bgu.spl.mics.application.parse;

public class JSONInput {
    private CameraJson Cameras;
    private LidarJson LiDarWorkers;
    private String poseJsonFile;
    private int TickTime;
    private int Duration;

    public CameraJson getCameras() {
        return Cameras;
    }

    public LidarJson getLiDarWorkers() {
        return LiDarWorkers;
    }

    public String getPoseJsonFile() {
        return poseJsonFile;
    }

    public int getTickTime() {
        return TickTime;
    }

    public int getDuration() {
        return Duration;
    }

    public void setCameras(CameraJson cameras) {
        Cameras = cameras;
    }

    public void setLiDarWorkers(LidarJson liDarWorkers) {
        LiDarWorkers = liDarWorkers;
    }

    public void setPoseJsonFile(String poseJsonFile) {
        this.poseJsonFile = poseJsonFile;
    }

    public void setTickTime(int tickTime) {
        TickTime = tickTime;
    }

    public void setDuration(int duration) {
        Duration = duration;
    }
}