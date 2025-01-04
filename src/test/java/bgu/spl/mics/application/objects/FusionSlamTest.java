package bgu.spl.mics.application.objects;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FusionSlamTest {
    private FusionSlam fusionSlam;
    List<CloudPoint> cloudPoints1 = new ArrayList<>();
    List<CloudPoint> cloudPoints2 = new ArrayList<>();
    List<CloudPoint> cloudPoints3 = new ArrayList<>();


    @BeforeEach
    void setUp() {
        fusionSlam = FusionSlam.getInstance();
        fusionSlam.getLandmarks().clear();

        cloudPoints1.add(new CloudPoint(0.1176, 3.6969));
        cloudPoints1.add(new CloudPoint(0.11362,3.6039));
        cloudPoints2.add(new CloudPoint(0.5, 3.9));
        cloudPoints2.add(new CloudPoint(0.2, 3.7));
        cloudPoints3.add(new CloudPoint(0.5, -2.1));
        cloudPoints3.add(new CloudPoint(0.8, -2.3));

        fusionSlam.getPoses().put(2,new Pose((float) -3.2076, (float) 0.0755,(float) -87.48,2)); //wall
        fusionSlam.getPoses().put(7,new Pose((float) -2.366, (float) 0.9327,(float)-28.08,7)); //door
        fusionSlam.getPoses().put(10,new Pose((float) 0.0, (float) 3.6,(float)57.3,10)); //wall
    }

    @Test
    void createLandMarks() {

        TrackedObject trackedObject1 = new TrackedObject("1", 2, "Wall near to chair", cloudPoints1);
        TrackedObject trackedObject2 = new TrackedObject("2", 7,"Door",cloudPoints3);
        TrackedObject trackedObject3 = new TrackedObject("1", 10, "Wall", cloudPoints2);
        List<TrackedObject> trackedObjects = Arrays.asList(trackedObject1,trackedObject2,trackedObject3);
        TrackedObjectsEvent event = new TrackedObjectsEvent(trackedObjects);

        fusionSlam.createLandMarks(event,10);

        LandMark landmark = fusionSlam.getLandmarks().get(1);
        assertEquals(-2.913332578606659, landmark.getCoordinates().get(0).getX());
        assertEquals(-1.1554635639732926, landmark.getCoordinates().get(0).getY());
        assertEquals(-2.742785996686237, landmark.getCoordinates().get(1).getX());
        assertEquals(-1.4731329886827864, landmark.getCoordinates().get(1).getY());

        LandMark landmark2 = fusionSlam.getLandmarks().get(0);
        assertEquals(-1.260438231426796, landmark2.getCoordinates().get(0).getX());
        assertEquals(3.124125914456589, landmark2.getCoordinates().get(0).getY());
        assertEquals(-1.3038657317253883, landmark2.getCoordinates().get(1).getX());
        assertEquals(2.9438188258406823, landmark2.getCoordinates().get(1).getY());
    }

    @Test
    void createLandMarks2() {
        cloudPoints2.add(new CloudPoint(0.1, 3.6));
        cloudPoints2.add(new CloudPoint(7.0, 8.0));

        TrackedObject trackedObject1 = new TrackedObject("1", 2, "Wall", cloudPoints1);
        TrackedObject trackedObject2 = new TrackedObject("2", 7,"Door",cloudPoints3);
        TrackedObject trackedObject3 = new TrackedObject("1", 10, "Wall", cloudPoints2);
        List<TrackedObject> trackedObjects = Arrays.asList(trackedObject1,trackedObject2,trackedObject3);
        TrackedObjectsEvent event = new TrackedObjectsEvent(trackedObjects);

        fusionSlam.createLandMarks(event, 7);

        LandMark landmark = fusionSlam.getLandmarks().get(1);
        assertEquals(-2.913332578606659, landmark.getCoordinates().get(0).getX());
        assertEquals(-1.1554635639732926, landmark.getCoordinates().get(0).getY());
        assertEquals(-2.742785996686237, landmark.getCoordinates().get(1).getX());
        assertEquals(-1.4731329886827864, landmark.getCoordinates().get(1).getY());

        LandMark landmark2 = fusionSlam.getLandmarks().get(0);
        assertEquals(-1.260438231426796, landmark2.getCoordinates().get(0).getX());
        assertEquals(3.124125914456589, landmark2.getCoordinates().get(0).getY());
        assertEquals(-1.3038657317253883, landmark2.getCoordinates().get(1).getX());
        assertEquals(2.9438188258406823, landmark2.getCoordinates().get(1).getY());

        List<LandMark> landmarks = fusionSlam.getLandmarks();
        assertEquals(4, landmarks.get(0).getCoordinates().size()); //check the number of the landmark coordinates
    }
}