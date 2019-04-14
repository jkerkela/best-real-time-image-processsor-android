package org.tensorflow.lite.examples.detection.tracking;

import android.app.Application;

import org.tensorflow.lite.examples.detection.tflite.Classifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class DetectedObjectTracker {

    private List<MultiBoxTracker.TrackedRecognition> detectedObjects = new ArrayList<>();
    private List<String> newDetections = new ArrayList<>();
    private NotificationHandler notificationHandler;

    DetectedObjectTracker(Application context) {
        this.notificationHandler = NotificationHandler.getNotificationHandler(context);
    }

    void handleDetection(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        setDetectionStatus(trackedRecognition, true);
        checkObjectDistance(trackedRecognition);
        updateDirection(trackedRecognition);
        if (!areAllObjectsOfTypeAlreadyTracked(trackedRecognition)) {
            handlePotentialNewObject(trackedRecognition);
        } else {
            updateDetectedObjectData(trackedRecognition);
        }
    }

    private boolean areAllObjectsOfTypeAlreadyTracked(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        return (getMatchingDetectedObjectsByType(trackedRecognition.title).size() >=
                getNumberOfMatches(newDetections, trackedRecognition.title));
    }

    private void setDetectionStatus(MultiBoxTracker.TrackedRecognition trackedRecognition, boolean status) {
        trackedRecognition.validDetection = status;
    }

    private List<MultiBoxTracker.TrackedRecognition> getMatchingDetectedObjectsByType(String objectType) {
        List<MultiBoxTracker.TrackedRecognition> objectList = new ArrayList<>();
        for(MultiBoxTracker.TrackedRecognition detectedObj : detectedObjects) {
            if(detectedObj.title.equals(objectType)) {
                objectList.add(detectedObj);
            }
        }
        return objectList;
    }

    private int getNumberOfMatches(List<String> list, String item) {
        return Collections.frequency(list, item);
    }

    private void checkObjectDistance(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        if (ObjectLocationProvider.isObjectInImmidiateProximity(trackedRecognition)) {
            notificationHandler.makeImmediateObjectProximityNotification(trackedRecognition);
        }
    }

    private void handlePotentialNewObject(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        if(detectedObjects.isEmpty() || !isObjectDetectedBefore(trackedRecognition)) {
            this.detectedObjects.add(trackedRecognition);
            notificationHandler.makeNewObjectNotification(trackedRecognition);
        }
    }

    private boolean isObjectDetectedBefore(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        List<MultiBoxTracker.TrackedRecognition> detectedMatchingObjectsByTitle =
                getMatchingDetectedObjectsByType(trackedRecognition.title);
        if (detectedMatchingObjectsByTitle.isEmpty()) {
            return true;
        }
        else return isObjectLocationNew(trackedRecognition, detectedMatchingObjectsByTitle);
    }

    private boolean isObjectLocationNew(MultiBoxTracker.TrackedRecognition trackedRecognition,
                                        List<MultiBoxTracker.TrackedRecognition> detectedObjects) {
        for (MultiBoxTracker.TrackedRecognition detectedObj : detectedObjects) {
            if (ObjectLocationProvider.doesDetectionRectLocationsDiffer(trackedRecognition.location, detectedObj.location)) {
                updateDetectedObjectData(trackedRecognition);
                return true;
            }
        }
        return false;
    }

    private void updateDirection(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        trackedRecognition.direction = ObjectLocationProvider.getObjectDirection(trackedRecognition).name();
    }

    private void updateDetectedObjectData(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        MultiBoxTracker.TrackedRecognition matchingObj = getMatchingObjectOrNull(trackedRecognition);
        if (matchingObj != null) {
            detectedObjects.remove(matchingObj);
        } else {
            List<MultiBoxTracker.TrackedRecognition> matchingObjsByType = getMatchingDetectedObjectsByType(trackedRecognition.title);
            MultiBoxTracker.TrackedRecognition deprecatedObj = matchingObjsByType.get(0);
            detectedObjects.remove(deprecatedObj);
        }
        detectedObjects.add(trackedRecognition);
    }

    private MultiBoxTracker.TrackedRecognition getMatchingObjectOrNull(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        for (MultiBoxTracker.TrackedRecognition detectedObj : detectedObjects) {
            if (!ObjectLocationProvider.doesDetectionRectLocationsDiffer(trackedRecognition.location, detectedObj.location)) {
                return detectedObj;
            }
        }
        return null;
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

    void remove(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        this.detectedObjects.remove(trackedRecognition);
    }

    void clearExpiredObjects() {
        Iterator<MultiBoxTracker.TrackedRecognition> iterator = detectedObjects.iterator();
        while (iterator.hasNext()) {
            MultiBoxTracker.TrackedRecognition detectedObj = iterator.next();
            if(!detectedObj.validDetection){
                iterator.remove();
            }
        }
    }
}
