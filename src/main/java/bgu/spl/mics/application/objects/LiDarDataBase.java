package bgu.spl.mics.application.objects;

import java.io.IOException;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.io.FileReader;

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

    private final List<StampedCloudPoints> listCloudPoints;
    private static LiDarDataBase instance;
    private final Object lock = new Object();

    private LiDarDataBase(List<StampedCloudPoints> cloudPointsList) {
        this.listCloudPoints = cloudPointsList;
    }

    public static LiDarDataBase getInstance(String filePath) {
        if (instance == null) {
            synchronized (LiDarDataBase.class) {
                if (instance == null) {
                    try {
                        // Load data from JSON file
                        Gson gson = new Gson();
                        Type stampedType = new TypeToken<List<StampedCloudPoints>>() {}.getType();
                        List<StampedCloudPoints> cloudPointsList = gson.fromJson(new FileReader(filePath), stampedType);

                        // Create the singleton instance with the loaded data
                        instance = new LiDarDataBase(cloudPointsList);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return instance;

    }

    public void addCloudPoint(StampedCloudPoints stampedCloudPoint) {
        synchronized (lock) {
            listCloudPoints.add(stampedCloudPoint);
        }
    }

    public List<StampedCloudPoints> getListCloudPoints() {
        synchronized (lock) {
            return listCloudPoints;
        }
    }
    public StampedCloudPoints getCloudPoint(String id) {
        synchronized (lock) {
            for (StampedCloudPoints cloudPoint : listCloudPoints) {
                if (cloudPoint.getId().equals(id)) {
                    return cloudPoint;
                }
            }
            return null;
        }
    }

    @Override
    public String toString() {
        synchronized (lock) {
            return "LiDarDataBase{" +
                    "cloudPoints=" + listCloudPoints +
                    '}';
        }
    }
}
