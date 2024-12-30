package bgu.spl.mics.application.parse;

public class CamerasConfigurations {
    private int id;
    private int frequency;
    private String camera_key;

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public String getCamera_key() {
        return camera_key;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setCamera_key(String camera_key) {
        this.camera_key = camera_key;
    }
}
