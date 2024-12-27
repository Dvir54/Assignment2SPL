package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

import java.util.List;

public class DetectObjectsEvent implements Event<Boolean> {

    private final StampedDetectedObjects stampedDetectedObjects;

    public DetectObjectsEvent(StampedDetectedObjects stampedDetectedObjects) {
        this.stampedDetectedObjects = stampedDetectedObjects;

    }

    public List<DetectedObject> getDetectedObjects() {
        return stampedDetectedObjects.getDetectedObjectsList();
    }

    public int getTime() {
        return stampedDetectedObjects.getTime();
    }
}
