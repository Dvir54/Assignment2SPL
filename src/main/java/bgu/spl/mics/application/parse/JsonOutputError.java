package bgu.spl.mics.application.parse;
import bgu.spl.mics.application.objects.*;
import com.google.gson.*;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JsonOutputError {
    private static StatisticalFolder statisticalFolder;
    private static FusionSlam fusionSlam;
    private static ConcurrentHashMap<Integer,Pose> poses;
    private static List<LandMark> landMarks;
    private static LinkedHashMap<String , StampedDetectedObjects> lastCamerasFrame = new LinkedHashMap<>();
    private static LinkedHashMap<String , List<TrackedObject>> lastLiDarWorkerTrackersFrame = new LinkedHashMap<>();

    public static void writeErrorToJson(String outputFilePath, List<Camera> cameras, List<LiDarWorkerTracker> LiDarWorkerTrackers) {

        statisticalFolder = StatisticalFolder.getInstance();
        fusionSlam = FusionSlam.getInstance();
        poses = fusionSlam.getPoses();
        landMarks = fusionSlam.getLandmarks();

        for (Camera camera : cameras) {
            String errorID = "camera" + camera.getId();
            StampedDetectedObjects stampedDetectedObjects = camera.getLastCameraFrame();
            lastCamerasFrame.put(errorID, stampedDetectedObjects);
        }

        for (LiDarWorkerTracker liDarWorkerTracker : LiDarWorkerTrackers) {
            String errorID = "LiDarWorkerTracker" + liDarWorkerTracker.getId();
            List<TrackedObject> trackedObjects = liDarWorkerTracker.getLastTrackedObjects();
            lastLiDarWorkerTrackersFrame.put(errorID, trackedObjects);
        }

        LinkedHashMap<String, Object> errorDetails = new LinkedHashMap<>();
        errorDetails.put("Error", fusionSlam.getErrorDescription());
        errorDetails.put("faultySensor", fusionSlam.getFaultySensor());
        errorDetails.put("lastCamerasFrame", lastCamerasFrame);
        errorDetails.put("lastLiDarWorkerTrackersFrame", lastLiDarWorkerTrackersFrame);
        errorDetails.put("poses", poses);
        errorDetails.put("statistics", statisticalFolder);
        errorDetails.put("landmarks", landMarks);

        // Serialize to JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(LinkedHashMap.class, new LinkedHashMapSerializer()) .create();
        JsonObject jsonOutput = gson.toJsonTree(errorDetails).getAsJsonObject();
        // Write to file
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            writer.write(gson.toJson(jsonOutput));
        } catch (IOException e) {
            System.err.println("Error writing JSON to file: " + e.getMessage());
        }
    }
    private static class LinkedHashMapSerializer implements JsonSerializer<LinkedHashMap<?, ?>> {
        @Override
        public JsonElement serialize(LinkedHashMap<?, ?> src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            for (Map.Entry<?, ?> entry : src.entrySet()) {
                jsonObject.add(String.valueOf(entry.getKey()), context.serialize(entry.getValue()));
            }
            return jsonObject;
        }
    }
}
