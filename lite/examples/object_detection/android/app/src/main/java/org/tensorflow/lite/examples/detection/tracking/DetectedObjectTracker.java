package org.tensorflow.lite.examples.detection.tracking;

import android.graphics.RectF;

import org.tensorflow.lite.examples.detection.env.Logger;

import java.util.ArrayList;
import java.util.List;

class DetectedObjectTracker {

    private static final Logger LOGGER = new Logger();
    private List<MultiBoxTracker.TrackedRecognition> detectedObjects = new ArrayList<>();;
    private final float HORIZONTAL_DIFF_FILTER_VALUE = 35.00f;
    private final float VERTICAL_DIFF_FILTER_VALUE = 70.00f;

    void addIfNotDetectedBefore(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        if(detectedObjects.isEmpty() || isObjectNotDetectedBefore(trackedRecognition)) {
            this.detectedObjects.add(trackedRecognition);
        }
    }

    //TODO: we should remove detected objects, when they are not on screen when coming in here
    void remove(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        this.detectedObjects.remove(trackedRecognition);
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
                LOGGER.d("DetectedObjectTracker, new tracked obj: " + trackedRecognition);
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

