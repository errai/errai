package org.jboss.workspace.client.framework;

public class NotificationCallback {
    private AcceptsCallback callback;
    private String message;

    private NotificationCallback next;

    public NotificationCallback(AcceptsCallback callback, String message) {
        this.callback = callback;
        this.message = message;
    }

    public AcceptsCallback getCallback() {
        return callback;
    }

    public void setCallback(AcceptsCallback callback) {
        this.callback = callback;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationCallback getNext() {
        return next;
    }

    public void call() {
        callback.callback(message, null);
        if (next != null) next.call();
    }

    public void addMessageCallback(NotificationCallback callback) {
        if (next == null) {
            next = callback;
            return;
        }

        NotificationCallback n = next;
        while (n.next != null) n = n.next;
        n.next = callback;
    }
}
