package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.GPSIMU;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {
    private final GPSIMU gpsimu;

    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("Gpsimu");
        this.gpsimu = gpsimu;
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class,(TickBroadcast tick) -> {
            int currenTime = tick.getCurrentTime();
            if(gpsimu.getStatus() == GPSIMU.Status.UP){
                if(currenTime < gpsimu.getPoseList().size()){
                    PoseEvent poseEvent = new PoseEvent(gpsimu.getPoseList().get(gpsimu.getCurrentTick()));
                    sendEvent(poseEvent);
                    gpsimu.setCurrentTick(currenTime + 1);
                }
                else {
                    gpsimu.setStatus(GPSIMU.Status.DOWN);
                    sendBroadcast(new TerminatedBroadcast("gpsimu terminated"));
                    terminate();
                }
            } else if (gpsimu.getStatus() == GPSIMU.Status.ERROR) {
                sendBroadcast(new CrashedBroadcast(gpsimu + " disconnected"));
                terminate();
            }
        });
        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminated) ->{
            if (terminated.getSenderId().equals("TimeService")){
                gpsimu.setStatus(GPSIMU.Status.DOWN);
                terminate();
            }
        });
        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crashed) ->{
            terminate();
        });
    }
}
