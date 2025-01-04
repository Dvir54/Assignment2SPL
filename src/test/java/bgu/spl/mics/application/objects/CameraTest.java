package bgu.spl.mics.application.objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class CameraTest {
    Camera camera;
    List<StampedDetectedObjects> detectedObjectsList = new ArrayList<>();


    @BeforeEach
    void setUp() {
        detectedObjectsList.add(new StampedDetectedObjects(1, Arrays.asList(new DetectedObject("Door", "Brown Door"), new DetectedObject("Wall_1", "Wall"))));
        detectedObjectsList.add(new StampedDetectedObjects(2, Arrays.asList(new DetectedObject("Dog", "Dog"))));
        camera = new Camera(1, 3, detectedObjectsList);
    }

    @Test
    void getStampedDetectedObject() {
        StampedDetectedObjects stamp = camera.getStampedDetectedObject(4);
        assertNotNull(stamp);
        assertEquals(detectedObjectsList.get(0), stamp);

        StampedDetectedObjects stamp2 = camera.getStampedDetectedObject(7);
        assertNull(stamp2);
    }

    @Test
    void getStampedDetectedObject2() {
        StampedDetectedObjects stampWithErrorObj = new StampedDetectedObjects(5, Arrays.asList(new DetectedObject("ERROR", "error"), new DetectedObject("dog", "blue dog")));
        camera.addStampedDetectedObject(stampWithErrorObj);

        StampedDetectedObjects errorStamp = camera.getStampedDetectedObject(8);
        assertNotEquals(null, errorStamp);
        assertEquals("error", errorStamp.checkIfError());
    }
}