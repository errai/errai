package org.jboss.errai.client.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MessageBusServiceAsync {
    void store(String subject, String message, AsyncCallback<Void> async);

    void nextMessage(AsyncCallback<String[]> async);

    void getSubjects(AsyncCallback<String[]> async);
}
