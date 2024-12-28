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
    private StatisticalFolder statisticalFolder;
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
                //should put here a  condition that if not exists, change the lidar's status to down
                int size = liDarWorkerTracker.getLiDarDataBase().getListCloudPoints().size()-1;
                if(currenTime <= liDarWorkerTracker.getLiDarDataBase().getListCloudPoints().get(size).getTime()){
                    if(liDarWorkerTracker.getLiDarDataBase().getListCloudPoints().get(currenTime).getId().equals("ERROR")){
                        sendBroadcast(new CrashedBroadcast("Sensor lidar"+ liDarWorkerTracker.getId() + " disconnected"));
                        terminate();
                    }
                    else{
                        statisticalFolder.incrementTrackedObjects(trackedObjects.size());
                        sendEvent(new TrackedObjectsEvent(trackedObjects));
                        //add the tracked objects to the lastTrackedObjects list
                        for (TrackedObject trackedObject : trackedObjects){
                            liDarWorkerTracker.addTrackedObject(trackedObject);
                        }
                        //statisticFolder
                        trackedObjects.clear();
                    }
                }
                else{
                    liDarWorkerTracker.setStatus(LiDarWorkerTracker.Status.DOWN);
                    sendBroadcast(new TerminatedBroadcast("Lidar"+ liDarWorkerTracker.getId() + " terminate"));
                    terminate();
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
                    StampedCloudPoints stampedCloudPoints = liDarWorkerTracker.getLiDarDataBase().getCloudPoint(id);
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
            terminate();
        });
        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crashed) ->{
            terminate();
        });
    }
}
//
