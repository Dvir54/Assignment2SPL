package bgu.spl.mics;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.services.CameraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import static org.junit.jupiter.api.Assertions.*;

class MessageBusImplTest {

    private MessageBusImpl messageBus;

    @BeforeEach
    public void setUp() {
        messageBus = MessageBusImpl.getInstance();
    }

    @Test
    void subscribeEvent() {
        MicroService microServiceCam = new CameraService(new Camera(1, 5, Arrays.asList(new StampedDetectedObjects(1,Arrays.asList(new DetectedObject("chair", "a blue chair"))))));
        messageBus.subscribeEvent(TrackedObjectsEvent.class, microServiceCam);
        BlockingQueue<MicroService> subscribers = messageBus.getEventServiceMap().get(TrackedObjectsEvent.class);
        assertTrue(subscribers.contains(microServiceCam), "MicroService is not subscribed to the Event");
    }

    @Test
    void subscribeBroadcast() {
        MicroService microServiceCam = new CameraService(new Camera(1, 5, Arrays.asList(new StampedDetectedObjects(1,Arrays.asList(new DetectedObject("chair", "a blue chair"))))));
        messageBus.subscribeBroadcast(CrashedBroadcast.class, microServiceCam);
        BlockingQueue<MicroService> subscribers = messageBus.getBroadcastServiceMap().get(CrashedBroadcast.class);
        assertTrue(subscribers.contains(microServiceCam), "MicroService is not subscribed to the Broadcast.");
    }

    @Test
    void register() {
        MicroService microServiceCam = new CameraService(new Camera(1, 5, Arrays.asList(new StampedDetectedObjects(1,Arrays.asList(new DetectedObject("chair", "a blue chair"))))));
        messageBus.register(microServiceCam);
        BlockingQueue<Message> servicesQueue = messageBus.getServiceMessageMap().get(microServiceCam);
        assertNotEquals(null, servicesQueue);
    }
}
