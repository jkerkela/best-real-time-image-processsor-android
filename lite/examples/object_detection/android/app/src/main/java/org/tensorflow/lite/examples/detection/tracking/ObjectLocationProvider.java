package org.tensorflow.lite.examples.detection.tracking;

import android.graphics.RectF;

class ObjectLocationProvider {

    private static final float VERTICAL_DIFF_FILTER_VALUE = 35.00f;
    private static final float HORIZONTAL_DIFF_FILTER_VALUE = 70.00f;
    private static final float IMMEDIATE_NEAR_ZONE = 50000.0f;
    private static final float VERTICAL_LEFT_DIRECTION_THRESHOLD = 350;
    private static final float VERTICAL_RIGHT_DIRECTION_THRESHOLD = 150;
    private static final float VERTICAL_FRONT_DIRECTION_THRESHOLD = 200;

    public enum Direction {
        IN_FRONT("Front"),
        LEFT("Left"),
        RIGHT("Right");

        private final String name;

        private Direction(String s) {
            name = s;
        }
    }

    static ObjectLocationProvider.Direction getObjectDirection(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        if (trackedRecognition.location.left < VERTICAL_FRONT_DIRECTION_THRESHOLD) {
            return ObjectLocationProvider.Direction.IN_FRONT;
        } else if (trackedRecognition.location.top < VERTICAL_RIGHT_DIRECTION_THRESHOLD) {
            return ObjectLocationProvider.Direction.RIGHT;
        } else if (trackedRecognition.location.top > VERTICAL_LEFT_DIRECTION_THRESHOLD) {
            return ObjectLocationProvider.Direction.LEFT;
        } else {
            return ObjectLocationProvider.Direction.IN_FRONT;
        }
    }

    static boolean isObjectInImmidiateProximity(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        Float relativeSizeOfObject = calculateRelativeAreaOfObject(trackedRecognition.location);
        return relativeSizeOfObject > IMMEDIATE_NEAR_ZONE;
    }

    private static Float calculateRelativeAreaOfObject(RectF rectF) {
        Float length = Math.abs(rectF.bottom - rectF.top);
        Float breadth = Math.abs(rectF.left - rectF.right);
        return length * breadth;
    }

    static boolean doesDetectionRectLocationsDiffer(RectF recognitionLocation, RectF detectedLocation) {
        return (doesValueDifferOverRedundancyFilter(recognitionLocation.bottom, detectedLocation.bottom, VERTICAL_DIFF_FILTER_VALUE) ||
                doesValueDifferOverRedundancyFilter(recognitionLocation.top, detectedLocation.top, VERTICAL_DIFF_FILTER_VALUE) ||
                doesValueDifferOverRedundancyFilter(recognitionLocation.left, detectedLocation.left, HORIZONTAL_DIFF_FILTER_VALUE) ||
                doesValueDifferOverRedundancyFilter(recognitionLocation.right, detectedLocation.right, HORIZONTAL_DIFF_FILTER_VALUE));
    }

    private static boolean doesValueDifferOverRedundancyFilter(float locationValue, float locationValueToCompareTo, Float filter) {
        return (Math.abs(locationValue - locationValueToCompareTo) > filter);
    }
}
