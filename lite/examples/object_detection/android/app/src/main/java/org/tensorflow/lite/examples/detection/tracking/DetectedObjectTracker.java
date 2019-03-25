package org.tensorflow.lite.examples.detection.tracking;

import android.graphics.RectF;

import org.tensorflow.lite.examples.detection.env.Logger;

import java.util.ArrayList;
import java.util.List;

public class DetectedObjectTracker {

    private static final Logger LOGGER = new Logger();
    private List<MultiBoxTracker.TrackedRecognition> detectedObjects = new ArrayList<>();;
    private final float HORIZONTAL_DIFF_FILTER_VALUE = 35.00f;
    private final float VERTICAL_DIFF_FILTER_VALUE = 70.00f;

    void addIfNotDetectedBefore(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        if(isObjectNotDetectedPreviously(trackedRecognition)) {
            this.detectedObjects.add(trackedRecognition);
        }
    }

    //TODO: when we should remove detected objects?; when we change direction by 90/120 degree slices(?)
    void remove(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        this.detectedObjects.remove(trackedRecognition);
    }

    private boolean isObjectNotDetectedPreviously(MultiBoxTracker.TrackedRecognition trackedRecognition) {
//        if (detectedObjects.isEmpty() || isObjectTypeNew(recognition.getTitle(), detectedObjects.values())) {
//            LOGGER.d("Detected NEW image reg by title, MappedREGS: " + detectedObjects);
//            return true;
//        }
        if (detectedObjects.isEmpty()) {
            LOGGER.d("DetectedObjectTracker, new tracked obj: " + trackedRecognition);
            return true;
        }
        return isObjectLocationNew(trackedRecognition.location);
    }

    private boolean isObjectLocationNew(RectF location) {
        for (MultiBoxTracker.TrackedRecognition detectedObj : detectedObjects) {
            //TODO: we need to support directional memory for objects
            if (doesDetectionRectLocationsDiffer(location, detectedObj.location)) {
                LOGGER.d("DetectedObjectTracker, new tracked obj: " + location);
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
        return (locationValue - locationValueToCompareTo > filter);
    }

//    private boolean isObjectTypeNew(String recognitionTitle, Collection<DetectedObjectTracker> detectedObjectTrackers) {
//        for (DetectedObjectTracker detectedObj : detectedObjectTrackers) {
//            if (detectedObj.getTitle().equals(recognitionTitle)) {
//                return false;
//            }
//        }
//        return true;
//    }

}
