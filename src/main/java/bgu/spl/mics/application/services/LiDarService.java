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
    private final String databasePath;
    private LiDarDataBase lidarDataBase;
    //Check if to change to blockingQueue



    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker, String databasePath) {
        super("Lidar" + LiDarWorkerTracker.getId());
        this.liDarWorkerTracker = LiDarWorkerTracker;
        this.trackedObjects = new ArrayList<>();
        this.sendTrackedObjects = new ArrayList<>();
        statisticalFolder = StatisticalFolder.getInstance();
        this.databasePath = databasePath;
        this.lidarDataBase = LiDarDataBase.getInstance(databasePath);

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
                if(currenTime <= lidarDataBase.getListCloudPoints().get(lidarDataBase.getListCloudPoints().size()-1).getTime() + liDarWorkerTracker.getFrequency()){
                    if(lidarDataBase.getListCloudPoints().get(currenTime).getId().equals("ERROR")){
                        liDarWorkerTracker.setStatus(LiDarWorkerTracker.Status.ERROR);
                        sendBroadcast(new CrashedBroadcast("lidar"+ liDarWorkerTracker.getId() + " disconnected"));
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
                }
                else {
                    liDarWorkerTracker.setStatus(LiDarWorkerTracker.Status.DOWN);
                    trackedObjects.clear();
                    sendBroadcast(new TerminatedBroadcast("lidar"+ liDarWorkerTracker.getId() + " terminated"));
                    terminate();
                }

            } else if (liDarWorkerTracker.getStatus() == LiDarWorkerTracker.Status.ERROR) {
                sendBroadcast(new CrashedBroadcast("lidar"+ liDarWorkerTracker.getId() + " disconnected"));
                terminate();
            }
        });
        subscribeEvent(DetectObjectsEvent.class, (DetectObjectsEvent event) ->{
            if (liDarWorkerTracker.getStatus() == LiDarWorkerTracker.Status.UP){
                for (DetectedObject detectedObject : event.getDetectedObjects()){
                    String id = detectedObject.getId();
                    StampedCloudPoints stampedCloudPoints = lidarDataBase.getCloudPoint(id);
                    //check if to change the time
                    TrackedObject trackedObject = new TrackedObject(id, event.getTime() + liDarWorkerTracker.getFrequency(), detectedObject.getDescription(), stampedCloudPoints.toCloudPointList());
                    trackedObjects.add(trackedObject);
                }
                complete(event, true);
            } else if (liDarWorkerTracker.getStatus() == LiDarWorkerTracker.Status.ERROR) {
                sendBroadcast(new CrashedBroadcast("lidar"+ liDarWorkerTracker.getId() + " disconnected"));
                terminate();
            }

        });
        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminated) ->{
            if(terminated.getSenderId().equals("TimeService")){
                liDarWorkerTracker.setStatus(LiDarWorkerTracker.Status.DOWN);
                trackedObjects.clear();
                terminate();
            }
        });
        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crashed) ->{
            terminate();
        });
    }
}
//
