package org.jboss.workspace.sampler.client.servicecontrol;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ServiceControlAsync {

    void startService(AsyncCallback<Void> async);

    void pauseService(AsyncCallback<Void> async);

    void stopService(AsyncCallback<Void> async);

    void getServiceStatus(AsyncCallback<Integer> async);

    void getName(AsyncCallback<String> async);
}
