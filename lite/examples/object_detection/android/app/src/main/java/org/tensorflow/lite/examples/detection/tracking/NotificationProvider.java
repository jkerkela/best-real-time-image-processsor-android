package org.tensorflow.lite.examples.detection.tracking;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;
import java.util.UUID;

public class NotificationProvider implements TextToSpeech.OnInitListener {

    private final Context context;
    private TextToSpeech myTTS;

    public NotificationProvider(Context context) {
        myTTS = new TextToSpeech(context, this);
        this.context = context;
    }

    public void makeImmediateObjectInDirectionProximityNotification() {
        makeVoiceNotification("Object in front in immediate proximity");
    }

    void makeImmediateObjectProximityNotification(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        makeVoiceNotification("Object: " + trackedRecognition.title + " in immediate proximity");
    }

    void makeNewObjectNotification(MultiBoxTracker.TrackedRecognition trackedRecognition) {
        makeVoiceNotification("New object: " + trackedRecognition.title + " on direction: "
                + trackedRecognition.direction);
    }

    private void makeVoiceNotification(String notification) {
        UUID uniqueIdentifier = UUID.randomUUID();
        myTTS.speak(notification, TextToSpeech.QUEUE_FLUSH, null, uniqueIdentifier.toString());
    }

    @Override
    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            if(myTTS.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
                myTTS.setLanguage(Locale.US);
        }
        else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(context, "Sorry! Text To Speech failed.", Toast.LENGTH_LONG).show();
        }
    }
}
