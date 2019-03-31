package org.tensorflow.lite.examples.detection.tracking;

import android.content.Context;
import android.graphics.RectF;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class DetectedObjectTracker {

    private Context context;
    private List<MultiBoxTracker.TrackedRecognition> detectedObjects = new ArrayList<>();
    private static final float HORIZONTAL_DIFF_FILTER_VALUE = 35.00f;
    private static final float VERTICAL_DIFF_FILTER_VALUE = 70.00f;
    private static final float IMMEDIATE_NEAR_ZONE = 50000.0f;

    private enum Direction {
        IN_FRONT("Front"),
        LEFT("Left"),
        RIGHT("Right");

        private final String name;

        private Direction(String s) {
            name = s;
        }
    }

    DetectedObjectTracker(Context context) {
        this.context = context;
    }
    void invalidateObjects() {
        for (MultiBoxTracker.TrackedRecognition detectedObj : detectedObjects) {
            setDetectionStatus(detectedObj, false);
        }
    }

    void handleDetection(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        setDetectionStatus(trackedRecognition, true);
        checkRelativeDistance(trackedRecognition);
        if(detectedObjects.isEmpty() || isObjectNotDetectedBefore(trackedRecognition)) {
            String direction = getObjectDirection(trackedRecognition);
            Toast.makeText(context, "New object: " + trackedRecognition.title + " on direction: " + direction, Toast.LENGTH_SHORT).show();
            this.detectedObjects.add(trackedRecognition);
        }
    }

    //TODO: works only in portrait mode
    private String getObjectDirection(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        if (trackedRecognition.location.top < 100) {
            return Direction.RIGHT.name;
        } else if (trackedRecognition.location.top > 400) {
            return Direction.LEFT.name;
        } else {
            return Direction.IN_FRONT.name;
        }
    }

    private void checkRelativeDistance(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        Float relativeSizeOfObject = calculateRelativeAreaOfObject(trackedRecognition.location);
        if(relativeSizeOfObject > IMMEDIATE_NEAR_ZONE) {
            Toast.makeText(context, "Object: " + trackedRecognition.title + " in immediate proximity",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private Float calculateRelativeAreaOfObject(RectF rectF) {
        Float length = Math.abs(rectF.bottom - rectF.top);
        Float breadth = Math.abs(rectF.left - rectF.right);
        return length * breadth;
    }

    void remove(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        this.detectedObjects.remove(trackedRecognition);
    }

    void clearExpiredObjects() {
        Iterator<MultiBoxTracker.TrackedRecognition> iterator = detectedObjects.iterator();
        while (iterator.hasNext()) {
            MultiBoxTracker.TrackedRecognition detectedObj = iterator.next();
            if(!detectedObj.validDetection){
                iterator.remove();
                Toast.makeText(context, "Object out of view: " + detectedObj.title, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isObjectNotDetectedBefore(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        List<MultiBoxTracker.TrackedRecognition> detectedMatchingObjects = getMatchingObjects(trackedRecognition.title);
        if (detectedMatchingObjects.isEmpty()) {
            return true;
        }
        else return isObjectLocationNew(trackedRecognition, detectedMatchingObjects);

    }

    private boolean isObjectLocationNew(MultiBoxTracker.TrackedRecognition trackedRecognition, List<MultiBoxTracker.TrackedRecognition> detectedObjects) {
        for (MultiBoxTracker.TrackedRecognition detectedObj : detectedObjects) {
            if (doesDetectionRectLocationsDiffer(trackedRecognition.location, detectedObj.location)) {
                return true;
            }
        }
        return false;
    }

    private boolean doesDetectionRectLocationsDiffer(RectF recognitionLocation, RectF detectedLocation) {
        return (doesValueDifferOverRedundancyFilter(recognitionLocation.bottom, detectedLocation.bottom, VERTICAL_DIFF_FILTER_VALUE) ||
                doesValueDifferOverRedundancyFilter(recognitionLocation.top, detectedLocation.top, VERTICAL_DIFF_FILTER_VALUE) ||
                doesValueDifferOverRedundancyFilter(recognitionLocation.left, detectedLocation.left, HORIZONTAL_DIFF_FILTER_VALUE) ||
                doesValueDifferOverRedundancyFilter(recognitionLocation.right, detectedLocation.right, HORIZONTAL_DIFF_FILTER_VALUE));
    }

    private boolean doesValueDifferOverRedundancyFilter(float locationValue, float locationValueToCompareTo, Float filter) {
        return (Math.abs(locationValue - locationValueToCompareTo) > filter);
    }

    private void setDetectionStatus(MultiBoxTracker.TrackedRecognition trackedRecognition, boolean status) {
        trackedRecognition.validDetection = status;
    }

    private List<MultiBoxTracker.TrackedRecognition> getMatchingObjects(String objectType) {
        List<MultiBoxTracker.TrackedRecognition> objectList = new ArrayList<>();
        for(MultiBoxTracker.TrackedRecognition detectedObj : detectedObjects) {
            if(detectedObj.title.equals(objectType)) {
                objectList.add(detectedObj);
            }
        }
        return objectList;
    }
}

