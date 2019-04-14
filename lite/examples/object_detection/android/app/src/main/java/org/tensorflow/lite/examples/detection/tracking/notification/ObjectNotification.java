package org.tensorflow.lite.examples.detection.tracking.notification;

public class ObjectNotification implements Comparable<ObjectNotification> {

    private final String message;
    private final Priority priority;

    public enum Priority {
        HIGH(1),
        MEDIUM(2),
        LOW(3);

        private final int value;

        private Priority(int i) {
            value = i;
        }
    }

    ObjectNotification(String message, Priority priority) {
        this.message = message;
        this.priority = priority;
    }

    @Override
    public int compareTo(ObjectNotification notificationToCompareTo) {
        if (this.priority.value > notificationToCompareTo.getPriority().value) { return 1; }
        else if (this.priority.value < notificationToCompareTo.getPriority().value) { return -1; }
        return 0;
    }

    String getMessage() {
        return message;
    }

    private Priority getPriority() {
        return priority;
    }

}
