package org.jboss.errai.client.rpc;

import com.google.gwt.user.client.rpc.RemoteService;

public interface MessageBusService extends RemoteService {
    public void store(String subject, String message);
    public String[] nextMessage();

//    public void remoteSubscribe(String subject);
//    public void remoteUnsubscribe(String subject);

    public String[] getSubjects();
}
