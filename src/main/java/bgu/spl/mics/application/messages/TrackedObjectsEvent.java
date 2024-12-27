package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;

import java.util.List;

public class TrackedObjectsEvent implements Event<Boolean> {

    private final List<TrackedObject> trackedObjectsList;

    public TrackedObjectsEvent(List<TrackedObject> trackedObjectsList) {
        this.trackedObjectsList = trackedObjectsList;
    }

    public List<TrackedObject> getTrackedObjectsList() {
        return trackedObjectsList;
        }


}
