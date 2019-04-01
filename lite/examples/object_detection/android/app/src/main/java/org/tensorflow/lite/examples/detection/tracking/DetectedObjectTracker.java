package org.tensorflow.lite.examples.detection.tracking;

import android.content.Context;
import android.graphics.RectF;
import android.widget.Toast;

import org.tensorflow.lite.examples.detection.tflite.Classifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class DetectedObjectTracker {

    private Context context;
    private List<MultiBoxTracker.TrackedRecognition> detectedObjects = new ArrayList<>();
    private List<String> newDetections = new ArrayList<>();
    private static final float VERTICAL_DIFF_FILTER_VALUE = 35.00f;
    private static final float HORIZONTAL_DIFF_FILTER_VALUE = 70.00f;
    private static final float IMMEDIATE_NEAR_ZONE = 50000.0f;

    //TODO: create class from this
    public enum Direction {
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

    void updateIncomingDetections(List<Classifier.Recognition> results) {
        newDetections.clear();
        for (Classifier.Recognition recognition : results) {
            newDetections.add(recognition.getTitle());
        }
    }

    void invalidateObjects() {
        for (MultiBoxTracker.TrackedRecognition detectedObj : detectedObjects) {
            setDetectionStatus(detectedObj, false);
        }
    }

    void handleDetection(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        setDetectionStatus(trackedRecognition, true);
        checkRelativeDistance(trackedRecognition);
        if (!areAllObjectsOfTypeAlreadyTracked(trackedRecognition)) {
            handlePotentialNewObject(trackedRecognition);
        }
        updateDirection(trackedRecognition);
    }

    private void handlePotentialNewObject(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        if(detectedObjects.isEmpty() || !isObjectDetectedBefore(trackedRecognition)) {
            this.detectedObjects.add(trackedRecognition);
            Toast.makeText(context, "New object: " + trackedRecognition.title + " on direction: "
                    + trackedRecognition.direction, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDirection(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        trackedRecognition.direction = getObjectDirection(trackedRecognition);
    }

    private boolean areAllObjectsOfTypeAlreadyTracked(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        return (getMatchingDetectedObjects(trackedRecognition.title).size() >=
                getNumberOfMatches(newDetections, trackedRecognition.title));
    }

    //TODO: works only in portrait mode
    private Direction getObjectDirection(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        if (trackedRecognition.location.left < 200) {
            return Direction.IN_FRONT;
        } else if (trackedRecognition.location.top < 150) {
            return Direction.RIGHT;
        } else if (trackedRecognition.location.top > 350) {
            return Direction.LEFT;
        } else {
            return Direction.IN_FRONT;
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

    private boolean isObjectDetectedBefore(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        List<MultiBoxTracker.TrackedRecognition> detectedMatchingObjectsByTitle =
                getMatchingDetectedObjects(trackedRecognition.title);
        if (detectedMatchingObjectsByTitle.isEmpty()) {
            return true;
        }
        else return isObjectLocationNew(trackedRecognition, detectedMatchingObjectsByTitle);

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

    private int getNumberOfMatches(List<String> list, String item) {
        return Collections.frequency(list, item);
    }

    private List<MultiBoxTracker.TrackedRecognition> getMatchingDetectedObjects(String objectType) {
        List<MultiBoxTracker.TrackedRecognition> objectList = new ArrayList<>();
        for(MultiBoxTracker.TrackedRecognition detectedObj : detectedObjects) {
            if(detectedObj.title.equals(objectType)) {
                objectList.add(detectedObj);
            }
        }
        return objectList;
    }
}
