package org.jboss.workspace.client.rpc;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.Set;

public interface MessageBusServiceAsync {
    void store(String subject, String message, AsyncCallback<Void> async);

    void nextMessage(AsyncCallback<String[]> async);

    void remoteSubscribe(String subject, AsyncCallback<Void> async);

    void getSubjects(AsyncCallback<String[]> async);
}
