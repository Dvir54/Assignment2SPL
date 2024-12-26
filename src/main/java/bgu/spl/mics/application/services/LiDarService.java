package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

/**
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarWorkerTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {
    LiDarWorkerTracker liDarWorkerTracker;
    StampedDetectedObjects stampedDetectedObjects;


    /**
     * Constructor for LiDarService.
     *
     * @param LiDarWorkerTracker A LiDAR Tracker worker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("Lidar" + LiDarWorkerTracker.getId());
        this.liDarWorkerTracker = LiDarWorkerTracker;
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
                if(currenTime <= liDarWorkerTracker.getLastTrackedObjects().get(liDarWorkerTracker.getLastTrackedObjects().size()-1).getTime()){
                    //send here trackedObjectsEvent to the fusionSlam
                }
                else{
                    liDarWorkerTracker.setStatus(LiDarWorkerTracker.Status.DOWN);
                    sendBroadcast(new TerminatedBroadcast("Lidar"+ liDarWorkerTracker.getId() + " is terminated"));
                    terminate();
                }
            } else if (liDarWorkerTracker.getStatus() == LiDarWorkerTracker.Status.ERROR) {
                sendBroadcast(new CrashedBroadcast("Lidar"+ liDarWorkerTracker.getId() + " is crashed"));
                terminate();
            }
        });
        subscribeEvent(DetectObjectsEvent.class, (DetectObjectsEvent e) ->{
            stampedDetectedObjects = new StampedDetectedObjects(e.getTime() + liDarWorkerTracker.getFrequency(), e.getDetectedObjects());
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
