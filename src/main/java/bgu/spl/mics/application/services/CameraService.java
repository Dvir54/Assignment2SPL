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
import bgu.spl.mics.application.objects.StatisticalFolder;

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
    private StatisticalFolder statisticalFolder;

    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("camera" + camera.getId());
        this.camera = camera;
        statisticalFolder = StatisticalFolder.getInstance();
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) ->{
            int currenTime = tick.getCurrentTime();
            if (camera.getStatus() == Camera.Status.UP) {
                //when the camera may have objects to detect at that specific tick
                for(StampedDetectedObjects detectedObjects : camera.getDetectedObjectsList()){
                    if(detectedObjects.getTime() + camera.getFrequency() == currenTime){
                        //check if I have an object with ID error
                        String description = detectedObjects.checkIfError();
                        if(description != null){
                            sendBroadcast(new CrashedBroadcast(description));
                            terminate();
                        }
                        else{
                            DetectObjectsEvent detectObjectsEvent = new DetectObjectsEvent(detectedObjects);
                            statisticalFolder.incrementDetectedObjects(detectedObjects.getDetectedObjectsList().size());
                            sendEvent(detectObjectsEvent);
                        }
                    }
                }
            } else if (camera.getStatus() == Camera.Status.ERROR) {
                sendBroadcast(new CrashedBroadcast("Sensor camera"+ camera.getId() + " disconnected"));
                terminate();
            }
        });
        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminated) ->{
            camera.setStatus(Camera.Status.DOWN);
            terminate();
        });
        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crashed) ->{
            terminate();
        });
    }
}
