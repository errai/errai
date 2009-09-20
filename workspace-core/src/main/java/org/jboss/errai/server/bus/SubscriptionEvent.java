package org.jboss.errai.server.bus;

public class SubscriptionEvent {
    private boolean disposeListener = false;
    private boolean remote = false;
    private Object sessionData;
    private String subject;

    public SubscriptionEvent(boolean remote, Object sessionData, String subject) {
        this.remote = remote;
        this.sessionData = sessionData;
        this.subject = subject;
    }

    public boolean isDisposeListener() {
        return disposeListener;
    }

    public void setDisposeListener(boolean disposeListener) {
        this.disposeListener = disposeListener;
    }

    public Object getSessionData() {
        return sessionData;
    }

    public void setSessionData(Object sessionData) {
        this.sessionData = sessionData;
    }

    public boolean isRemote() {
        return remote;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
