package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    private ConcurrentLinkedQueue<DetectObjectsEvent> detectObjectsEvents;
    private ArrayList<TrackedObject> sendTrackedObjects;
    private StatisticalFolder statisticalFolder;
    private final String databasePath;
    private final LiDarDataBase lidarDataBase;

    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */

    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker, String databasePath) {
        super("Lidar" + LiDarWorkerTracker.getId());
        this.liDarWorkerTracker = LiDarWorkerTracker;
        this.sendTrackedObjects = new ArrayList<>();
        statisticalFolder = StatisticalFolder.getInstance();
        this.databasePath = databasePath;
        this.lidarDataBase = LiDarDataBase.getInstance(databasePath);
        this.detectObjectsEvents = new ConcurrentLinkedQueue<>();

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
                if (currenTime <= lidarDataBase.getListCloudPoints().get(lidarDataBase.getListCloudPoints().size() - 1).getTime() + liDarWorkerTracker.getFrequency()) {
                    if(liDarWorkerTracker.checkIfError(currenTime)){
                        liDarWorkerTracker.setStatus(LiDarWorkerTracker.Status.ERROR);
                        sendBroadcast(new CrashedBroadcast("lidar"+liDarWorkerTracker.getId(), "Lidar disconnected"));
                        terminate();
                    }
                    else {
                        sendTrackedObjects = liDarWorkerTracker.getSendedTrackedObjects(detectObjectsEvents, currenTime);
                        for(DetectObjectsEvent detectObjectsEvent : liDarWorkerTracker.getDoneDetectObjectsEvents()){
                            detectObjectsEvents.remove(detectObjectsEvent);
                            complete(detectObjectsEvent, true);
                        }
                        if(!sendTrackedObjects.isEmpty()){
                            statisticalFolder.incrementTrackedObjects(sendTrackedObjects.size());
                            liDarWorkerTracker.setLastTrackedObjects(sendTrackedObjects);
                            TrackedObjectsEvent trackedObjectsEvent = new TrackedObjectsEvent(sendTrackedObjects);
                            sendEvent(trackedObjectsEvent);
                        }
                    }
                }else {
                    liDarWorkerTracker.setStatus(LiDarWorkerTracker.Status.DOWN);
                    sendBroadcast(new TerminatedBroadcast("lidar"+liDarWorkerTracker.getId()+ "terminated"));
                    terminate();
                }

            }
        });
        subscribeEvent(DetectObjectsEvent.class, (DetectObjectsEvent event) ->{
            if (liDarWorkerTracker.getStatus() == LiDarWorkerTracker.Status.UP){
                detectObjectsEvents.add(event);
            } else if (liDarWorkerTracker.getStatus() == LiDarWorkerTracker.Status.ERROR) {
                sendBroadcast(new CrashedBroadcast("lidar"+liDarWorkerTracker.getId(), "Lidar disconnected"));
                terminate();
            }

        });
        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminated) ->{
            if(terminated.getSenderId().equals("TimeService")){
                liDarWorkerTracker.setStatus(LiDarWorkerTracker.Status.DOWN);
                terminate();
            }
        });
        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crashed) ->{
            sendBroadcast(new CrashedBroadcast("lidar"+liDarWorkerTracker.getId(), "Lidar disconnected"));
            terminate();
        });
    }
}