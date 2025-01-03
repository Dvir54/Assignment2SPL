package bgu.spl.mics.application;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.parse.CamerasConfigurations;
import bgu.spl.mics.application.parse.JSONInput;
import bgu.spl.mics.application.parse.LidarConfigurations;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import static bgu.spl.mics.application.parse.JsonOutput.writeOutputToJson;
import static bgu.spl.mics.application.parse.JsonOutputError.writeErrorToJson;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */

public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */

    public static void main(String[] args) {
        // TODO: Parse configuration file.
        // TODO: Initialize system components and services.
        String filePath = args[0];
        String filebase = Paths.get(filePath).getParent().toFile().getAbsolutePath();

        MessageBusImpl messageBus = MessageBusImpl.getInstance();
        FusionSlam fusionSlam = FusionSlam.getInstance();

        List<Camera> camerasList = new ArrayList<>();
        List<LiDarWorkerTracker> LiDarWorkerList = new ArrayList<>();
        List<Pose> posesList = new ArrayList<>();
        List<MicroService> microServices = new ArrayList<>();
        TimeService timeService;

        //reading all the jsons files
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            // Parse the JSON file into a Java object
            JSONInput input = gson.fromJson(reader, JSONInput.class);

            //build a map for building a camera object
            String cameraDataPath = filebase + input.getCameras().getCamera_datas_path();
            try (FileReader cameraPathReader = new FileReader(cameraDataPath)) {
                // Define the type for a Map<String, List<StampedDetectedObjects>>
                Type mapType = new TypeToken<ConcurrentHashMap<String, List<StampedDetectedObjects>>>() {
                }.getType();
                ConcurrentHashMap<String, List<StampedDetectedObjects>> camDataMap = gson.fromJson(cameraPathReader, mapType);

                //build all the cameras
                CamerasConfigurations[] cameras = input.getCameras().getCamerasConfigurations();
                for (CamerasConfigurations camera : cameras) {
                    List<StampedDetectedObjects> detectedObjects = camDataMap.get(camera.getCamera_key());
                    Camera newCamera = new Camera(camera.getId(), camera.getFrequency(), detectedObjects);
                    camerasList.add(newCamera);
                }

            } catch (IOException e) {
                System.out.println("Error reading the file: " + e.getMessage());
            }

            //json for lidar_data_base
            String dataPath = filebase + input.getLiDarWorkers().getLidars_data_path();

            //build all the lidar_workers
            LidarConfigurations[] lidars = input.getLiDarWorkers().getLidarConfigurations();
            for (LidarConfigurations lidar : lidars) {
                LiDarWorkerTracker newLiDarWorkerTracker = new LiDarWorkerTracker(lidar.getId(), lidar.getFrequency(), dataPath);
                LiDarWorkerList.add(newLiDarWorkerTracker);
            }

            //build the posses list for GPSiMU
            String poseJsonFile = filebase + input.getPoseJsonFile();
            try (FileReader poseReader = new FileReader(poseJsonFile)) {
                // Define the type for a List<Pose>
                Type listType = new TypeToken<List<Pose>>() {
                }.getType();
                posesList = gson.fromJson(poseReader, listType);
            } catch (IOException e) {
                System.out.println("Error reading the file: " + e.getMessage());
            }

            //build GPSiMU
            GPSIMU gpsimu = new GPSIMU(posesList);

            // Register Microservices

            FusionSlamService fusionSlamService = new FusionSlamService(fusionSlam);
            messageBus.register(fusionSlamService);
            microServices.add(fusionSlamService);

            PoseService poseService = new PoseService(gpsimu);
            messageBus.register(poseService);
            microServices.add(poseService);
            fusionSlam.addMicroService();

            for (Camera camera : camerasList) {
                CameraService cameraService = new CameraService(camera);
                messageBus.register(cameraService);
                microServices.add(cameraService);
                fusionSlam.addMicroService();
            }

            for (LiDarWorkerTracker lidarWorker : LiDarWorkerList) {
                LiDarService lidarService = new LiDarService(lidarWorker, dataPath);
                messageBus.register(lidarService);
                microServices.add(lidarService);
                fusionSlam.addMicroService();
            }

            timeService = new TimeService(input.getTickTime(), input.getDuration());
            messageBus.register(timeService);
            microServices.add(timeService);


        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
        }

        // TODO: Start the simulation.
        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < microServices.size(); i++) {
            Thread t = new Thread(microServices.get(i));
            threadList.add(t);
            t.start();
        }

        for (Thread t : threadList) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrupted: " + e.getMessage());
            }

        }

        if(!fusionSlam.getIsCrashed()){
            writeOutputToJson("./output.json");
        }
        else{
            writeErrorToJson("./error.json", camerasList, LiDarWorkerList);
        }
    }
}