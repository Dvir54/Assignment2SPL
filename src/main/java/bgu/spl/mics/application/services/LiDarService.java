package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;

import java.util.ArrayList;
import java.util.Objects;


/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {
    private final LiDarWorkerTracker liDarWorkerTracker;
    private ArrayList<TrackedObject> trackedObjects;
    private ArrayList<TrackedObject> sendTrackedObjects;
    private StatisticalFolder statisticalFolder;
    private LiDarDataBase lidarDataBase = LiDarDataBase.getInstance("src/main/LidarData.json");
    //Check if to change to blockingQueue



    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("Lidar" + LiDarWorkerTracker.getId());
        this.liDarWorkerTracker = LiDarWorkerTracker;
        this.trackedObjects = new ArrayList<>();
        this.sendTrackedObjects = new ArrayList<>();
        statisticalFolder = StatisticalFolder.getInstance();
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) ->{
            int currenTime = tick.getCurrentTime();
            if (liDarWorkerTracker.getStatus() == LiDarWorkerTracker.Status.UP) {
                if(lidarDataBase.getListCloudPoints().get(currenTime).getId().equals("ERROR")){
                    sendBroadcast(new CrashedBroadcast("Sensor lidar"+ liDarWorkerTracker.getId() + " disconnected"));
                    terminate();
                }
                else{
                    for (TrackedObject trackedObject : trackedObjects){
                        if(trackedObject.getTime() <= currenTime){
                            sendTrackedObjects.add(trackedObject);
                            trackedObjects.remove(trackedObject);
                        }
                    }
                    statisticalFolder.incrementTrackedObjects(sendTrackedObjects.size());
                    sendEvent(new TrackedObjectsEvent(sendTrackedObjects));
                    //add the tracked objects to the lastTrackedObjects list
                    for (TrackedObject trackedObject : sendTrackedObjects){
                        liDarWorkerTracker.addTrackedObject(trackedObject);
                    }
                    sendTrackedObjects.clear();
                }
            } else if (liDarWorkerTracker.getStatus() == LiDarWorkerTracker.Status.ERROR) {
                sendBroadcast(new CrashedBroadcast("Sensor lidar"+ liDarWorkerTracker.getId() + " disconnected"));
                terminate();
            }
        });
        subscribeEvent(DetectObjectsEvent.class, (DetectObjectsEvent e) ->{
            if (liDarWorkerTracker.getStatus() == LiDarWorkerTracker.Status.UP){
                for (DetectedObject detectedObject : e.getDetectedObjects()){
                    String id = detectedObject.getId();
                    StampedCloudPoints stampedCloudPoints = lidarDataBase.getCloudPoint(id);
                    TrackedObject trackedObject = new TrackedObject(id, e.getTime() + liDarWorkerTracker.getFrequency(), detectedObject.getDescription(), stampedCloudPoints.toCloudPointList());
                    trackedObjects.add(trackedObject);
                }
                complete(e, true);
            } else if (liDarWorkerTracker.getStatus() == LiDarWorkerTracker.Status.ERROR) {
                sendBroadcast(new CrashedBroadcast("Sensor lidar"+ liDarWorkerTracker.getId() + " disconnected"));
                terminate();
            }

        });
        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminated) ->{
            trackedObjects.clear();
            liDarWorkerTracker.setStatus(LiDarWorkerTracker.Status.DOWN);
            terminate();
        });
        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crashed) ->{
            terminate();
        });
    }
}
//
