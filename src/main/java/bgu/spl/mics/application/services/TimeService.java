package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */

public class TimeService extends MicroService {
    private final int tickTime;
    private final int duration;
    private int currentTick;
    StatisticalFolder statisticalFolder;

    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */

    public TimeService(int TickTime, int Duration) {
        super("Time");
        this.tickTime = TickTime;
        this.duration = Duration;
        this.currentTick = 1;
        statisticalFolder = StatisticalFolder.getInstance();
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */

    @Override
    protected void initialize() {
        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminatedBroadcast) -> {
            if(terminatedBroadcast.getSenderId().equals("FusionSlam terminated")){
                terminate();
            }
        });

        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crashedBroadcast) -> {
            if(crashedBroadcast.getSenderId().equals("FusionSlam")) {
                terminate();
            }
        });

        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) ->{
            try {
                if (currentTick <= duration){
                    currentTick = currentTick +1;
                    statisticalFolder.incrementSystemRuntime(1);
                    sendBroadcast(new TickBroadcast(currentTick));
                    Thread.sleep(tickTime);
                }
                else {
                    sendBroadcast(new TerminatedBroadcast("TimeService"));
                    terminate();
                }
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        sendBroadcast(new TickBroadcast(currentTick));
    }
}
