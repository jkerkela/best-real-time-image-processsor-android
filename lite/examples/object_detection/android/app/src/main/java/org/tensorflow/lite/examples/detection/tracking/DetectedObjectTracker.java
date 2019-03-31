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
            this.detectedObjects.add(trackedRecognition);
        }
    }

    private void checkRelativeDistance(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        Float relativeSizeOfObject = calculateRelativeAreaOfObject(trackedRecognition.location);
        Float immediateNearZone = 50000.0f;
        if(relativeSizeOfObject > immediateNearZone) {
            Toast.makeText(context, "Object: " + trackedRecognition.title + " very near",
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
                Toast.makeText(context, "New object: " + trackedRecognition.title, Toast.LENGTH_SHORT).show();
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

