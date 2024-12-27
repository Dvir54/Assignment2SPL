package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Pose;

public class PoseEvent implements Event<Boolean> {

    private final Pose currPose;

    public PoseEvent(Pose currPose) {
        this.currPose = currPose;
    }

    public Pose getCurrPose() {
        return currPose;
    }
}
