package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    private final FusionSlam fusionSlam;
    private ConcurrentLinkedQueue<TrackedObjectsEvent> trackedObjectEventList;
    private List<LandMark> updateLandMarks;
    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam) {
        super("FusionSlam");
        this.fusionSlam = fusionSlam;
        this.trackedObjectEventList = new ConcurrentLinkedQueue<>();
        updateLandMarks = new ArrayList<>();
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) ->{
            int currTick = tick.getCurrentTime();
            for(TrackedObjectsEvent trackedObjectsEvent : trackedObjectEventList){
                fusionSlam.createLandMarks(trackedObjectsEvent, currTick);

                trackedObjectEventList.remove(trackedObjectsEvent);
                complete(trackedObjectsEvent, true);
            }
        });

        subscribeEvent(PoseEvent.class, (PoseEvent poseEvent) ->{
            Pose pose = poseEvent.getCurrPose();
            fusionSlam.addPose(pose);
            complete(poseEvent, true);
        });

        subscribeEvent(TrackedObjectsEvent.class, (TrackedObjectsEvent trackedObjectsEvent) ->{
            trackedObjectEventList.add(trackedObjectsEvent);
        });

        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminated) ->{
            if(terminated.getSenderId().equals("TimeService")){
                terminate();
            }
            else if (fusionSlam.getCountMicroServices() == 1){
                fusionSlam.reduceMicroService();
                sendBroadcast(new TerminatedBroadcast("FusionSlam terminated"));
                terminate();
            }
            else{
                fusionSlam.reduceMicroService();
            }

        });
        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crashed) ->{
            terminate();
        });
    }
}
