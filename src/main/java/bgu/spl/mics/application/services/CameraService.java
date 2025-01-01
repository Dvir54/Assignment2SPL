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
            //check if the camera is up, otherwise the camera is Error and the every other service should terminate
            if (camera.getStatus() == Camera.Status.UP) {
                //check if the current time is within the range of the camera's detected objects, otherwise the camera is down
                if(currenTime <= camera.getDetectedObjectsList().get(camera.getDetectedObjectsList().size()-1).getTime()){
                    StampedDetectedObjects stampedDetectedObjects = camera.getStampedDetectedObject(currenTime);
                    //check if the camera detects objects at that tick, otherwise do nothing
                    if (stampedDetectedObjects != null && (stampedDetectedObjects.getDetectedObjectsList() != null) && !(stampedDetectedObjects.getDetectedObjectsList().isEmpty())) {
                        String description = camera.checkError(stampedDetectedObjects);
                        // check if the camera detected an error, otherwise send the detected objects
                        if( description != null){
                            camera.setStatus(Camera.Status.ERROR);
                            sendBroadcast(new CrashedBroadcast(description));
                            terminate();
                        }
                        else{
                            DetectObjectsEvent detectObjectsEvent = new DetectObjectsEvent(stampedDetectedObjects);
                            statisticalFolder.incrementDetectedObjects(stampedDetectedObjects.getDetectedObjectsList().size());
                            //print check
                            int actualTime = currenTime - camera.getFrequency();
                            System.out.println("camera"+camera.getId()+" send objects at time "+currenTime + "  that detected at time:  " + actualTime);
                            sendEvent(detectObjectsEvent);
                        }
                    }

                }
                else {
                    camera.setStatus(Camera.Status.DOWN);
                    sendBroadcast(new TerminatedBroadcast("camera"+camera.getId()+ "terminated"));
                    terminate();
                }
            } else if (camera.getStatus() == Camera.Status.ERROR) {
                sendBroadcast(new CrashedBroadcast("camera"+ camera.getId() + " disconnected"));
                terminate();

            }
        });
        subscribeBroadcast(TerminatedBroadcast.class, (TerminatedBroadcast terminated) ->{
            if(terminated.getSenderId().equals("TimeService")){
                terminate();
            }
        });
        subscribeBroadcast(CrashedBroadcast.class, (CrashedBroadcast crashed) ->{
            terminate();
        });
    }
}
