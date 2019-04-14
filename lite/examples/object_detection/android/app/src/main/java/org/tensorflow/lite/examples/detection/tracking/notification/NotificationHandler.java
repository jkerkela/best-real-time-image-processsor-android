package org.tensorflow.lite.examples.detection.tracking.notification;

import android.app.Application;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import org.tensorflow.lite.examples.detection.tracking.MultiBoxTracker;

import java.util.Iterator;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.UUID;

public class NotificationHandler implements TextToSpeech.OnInitListener {

    private PriorityQueue<ObjectNotification> objectNotificationPriorityQueue = new PriorityQueue<>();
    private final Application context;
    private static NotificationHandler notificationHandler;
    private TextToSpeech textToSpeech;

    private Handler handler = new Handler();
    private final int delayInMilliSeconds = 5000;
    private String lastNotification;

    private NotificationHandler(Application context) {
        textToSpeech = new TextToSpeech(context, this);
        this.context = context;
        handler.postDelayed(new Runnable(){
            public void run(){
                postVoiceNotificationsFromQueue();
                handler.postDelayed(this, delayInMilliSeconds);
            }
        }, delayInMilliSeconds);
    }

    private void postVoiceNotificationsFromQueue() {
        ObjectNotification notificationToPost = getNewNotificationOrNull();
        if(notificationToPost != null) {
            makeVoiceNotification(notificationToPost.getMessage());
        }
        objectNotificationPriorityQueue.clear();
    }

    private ObjectNotification getNewNotificationOrNull() {
        ObjectNotification objectNotification = null;
        Iterator<ObjectNotification> iterator = objectNotificationPriorityQueue.iterator();
        while (iterator.hasNext()) {
            ObjectNotification objNotification = iterator.next();
            if(!objNotification.getMessage().equals(lastNotification)){
                return objNotification;
            }
            iterator.remove();
        }
        return objectNotification;
    }

    public static NotificationHandler getNotificationHandler(Application context) {
        if (notificationHandler == null) { notificationHandler = new NotificationHandler(context); }
        return notificationHandler;
    }

    public void makeImmediateObjectInDirectionProximityNotification() {
        ObjectNotification notification = new ObjectNotification(
                "Object in front in immediate proximity",
                ObjectNotification.Priority.HIGH);
        addNotificationToQueue(notification);
    }

    private void addNotificationToQueue(ObjectNotification notification) {
        objectNotificationPriorityQueue.add(notification);
    }

    public void makeImmediateObjectProximityNotification(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        ObjectNotification notification = new ObjectNotification(
                "Object: " + trackedRecognition.title + " in immediate proximity",
                ObjectNotification.Priority.HIGH);
        addNotificationToQueue(notification);
    }

    public void makeNewObjectNotification(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        ObjectNotification notification = new ObjectNotification(
                "New object: " + trackedRecognition.title + " on direction: " + trackedRecognition.direction,
                ObjectNotification.Priority.LOW);
        addNotificationToQueue(notification);
    }

    private void makeVoiceNotification(String notification) {
        UUID uniqueIdentifier = UUID.randomUUID();
        this.lastNotification = notification;
        textToSpeech.speak(notification, TextToSpeech.QUEUE_FLUSH, null, uniqueIdentifier.toString());
    }

    @Override
    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            if(textToSpeech.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
                textToSpeech.setLanguage(Locale.US);
        }
        else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(context, "Sorry! Text To Speech failed.", Toast.LENGTH_LONG).show();
        }
    }
}
