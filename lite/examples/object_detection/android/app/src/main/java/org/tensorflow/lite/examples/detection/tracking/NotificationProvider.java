package org.tensorflow.lite.examples.detection.tracking;

import android.content.Context;
import android.widget.Toast;

class NotificationProvider {

    private final Context context;

    NotificationProvider(Context context) {
        this.context = context;
    }

    void makeImmediateObjectProximityNotification(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        Toast.makeText(context, "Object: " + trackedRecognition.title + " in immediate proximity",
                Toast.LENGTH_SHORT).show();
    }

    void makeNewObjectNotification(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        Toast.makeText(context, "New object: " + trackedRecognition.title + " on direction: "
                + trackedRecognition.direction, Toast.LENGTH_SHORT).show();
    }
}
