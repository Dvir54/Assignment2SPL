package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.*;

import java.util.ArrayList;
import java.util.List;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {
    private final FusionSlam fusionSlam;
    private List<LandMark> updateLandMarks;
    private StatisticalFolder statisticalFolder;
    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */
    public FusionSlamService(FusionSlam fusionSlam) {
        super("FusionSlam");
        this.fusionSlam = fusionSlam;
        updateLandMarks = new ArrayList<>();
        statisticalFolder = StatisticalFolder.getInstance();
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) ->{
            for (LandMark updateLandMark : updateLandMarks){
                fusionSlam.updateLandmark(updateLandMark);
                statisticalFolder.incrementLandmarks(fusionSlam.getCountNewLandMarks());
            }
            updateLandMarks.clear();
        });

        subscribeEvent(PoseEvent.class, (PoseEvent poseEvent) ->{
            Pose pose = poseEvent.getCurrPose();
            fusionSlam.addPose(pose);
            complete(poseEvent, true);
        });

        subscribeEvent(TrackedObjectsEvent.class, (TrackedObjectsEvent trackedObjectsEvent) ->{
            List<TrackedObject> trackedObjects = trackedObjectsEvent.getTrackedObjectsList();
            for (TrackedObject trackedObject : trackedObjects){
                Pose pose = fusionSlam.getPoses().get(trackedObject.getTime());
                List<CloudPoint> list = trackedObject.calculateGlobalCoordinates(pose.getX(), pose.getY(), pose.getYaw());
                LandMark updateLandMark = new LandMark(trackedObject.getId(), trackedObject.getDescription(), list);
                updateLandMarks.add(updateLandMark);
                complete(trackedObjectsEvent, true);
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
