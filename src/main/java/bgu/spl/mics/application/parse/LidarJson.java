package bgu.spl.mics.application.parse;

public class LidarJson {
    private LidarConfigurations[] LidarConfigurations;
    private String lidars_data_path;

    public LidarConfigurations[] getLidarConfigurations() {
        return LidarConfigurations;
    }
    public String getLidars_data_path() {
        return lidars_data_path;
    }
    public void setLidarConfigurations(LidarConfigurations[] lidarConfigurations) {
        LidarConfigurations = lidarConfigurations;
    }
    public void setLidars_data_path(String lidars_data_path) {
        this.lidars_data_path = lidars_data_path;
    }
}
