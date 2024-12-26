package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

import java.util.List;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {
    private final Camera camera;

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("camera" + camera.getId());
        this.camera = camera;
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        //updates the detection time to time + frequency
        for (StampedDetectedObjects detectedObjects : camera.getDetectedObjectsList()){
            detectedObjects.setTime(camera.getFrequency());
        }
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) ->{
            int currenTime = tick.getCurrentTime();
            if (camera.getStatus() == Camera.Status.UP) {
                //when the camera may have objects to detect at that specific tick
                if(currenTime <= camera.getDetectedObjectsList().get(camera.getDetectedObjectsList().size()-1).getTime()){
                    for(StampedDetectedObjects detectedObjects : camera.getDetectedObjectsList()){
                        if(detectedObjects.getTime() == currenTime){
                            DetectObjectsEvent detectObjectsEvent = new DetectObjectsEvent(detectedObjects);
                            sendEvent(detectObjectsEvent);
                        }
                    }
                }
                else{
                    camera.setStatus(Camera.Status.DOWN);
                    sendBroadcast(new TerminatedBroadcast("camera"+ camera.getId() + " is terminated"));
                    terminate();
                }
            } else if (camera.getStatus() == Camera.Status.ERROR) {
                sendBroadcast(new CrashedBroadcast("camera"+ camera.getId() + " is crashed"));
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
