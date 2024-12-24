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

    private LiDarDataBase() {
        this.cloudPoints = new ArrayList<>();
    }

    public static synchronized LiDarDataBase getInstance(String filePath) {
        if (instance == null) {
            instance = new LiDarDataBase();
            // TODO: Load data from file at filePath if needed
        }
        return instance;
    }

    public List<StampedCloudPoints> getCloudPoints() {
        return cloudPoints;
    }

    public void addCloudPoint(StampedCloudPoints stampedCloudPoint) {
        cloudPoints.add(stampedCloudPoint);
    }

    @Override
    public String toString() {
        return "LiDarDataBase{" +
                "cloudPoints=" + cloudPoints +
                '}';
    }
}
