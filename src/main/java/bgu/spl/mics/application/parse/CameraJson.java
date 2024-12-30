package bgu.spl.mics.application.parse;

public class CameraJson {
    private CamerasConfigurations[] CamerasConfigurations;
    private String camera_datas_path;

    public CamerasConfigurations[] getCamerasConfigurations() {
        return CamerasConfigurations;
    }

    public String getCamera_datas_path() {
        return camera_datas_path;
    }

    public void setCamerasConfigurations(CamerasConfigurations[] camerasConfigurations) {
        CamerasConfigurations = camerasConfigurations;
    }

    public void setCamera_datas_path(String camera_datas_path) {
        this.camera_datas_path = camera_datas_path;
    }
}