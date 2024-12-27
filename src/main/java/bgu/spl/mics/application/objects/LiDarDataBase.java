package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */

    private final List<StampedCloudPoints> cloudPoints;
    private static LiDarDataBase instance;
    private final Object lock = new Object();

    private LiDarDataBase() {
        this.cloudPoints = new ArrayList<>();
    }

    public static LiDarDataBase getInstance(String filePath) {
        if (instance == null) {
            instance = new LiDarDataBase();
            // TODO: Load data from file at filePath if needed
        }
        return instance;
    }
    public static LiDarDataBase getInstance() {
        if (instance == null) {
            instance = new LiDarDataBase();
        }
        return instance;
    }

    public List<StampedCloudPoints> getCloudPoints() {
        synchronized (lock) {
            return cloudPoints;
        }
    }

    public void addCloudPoint(StampedCloudPoints stampedCloudPoint) {
        synchronized (lock) {
            cloudPoints.add(stampedCloudPoint);
        }
    }

    @Override
    public String toString() {
        synchronized (lock) {
            return "LiDarDataBase{" +
                    "cloudPoints=" + cloudPoints +
                    '}';
        }
    }
}
