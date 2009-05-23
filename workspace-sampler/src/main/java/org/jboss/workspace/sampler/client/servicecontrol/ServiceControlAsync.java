package org.jboss.workspace.sampler.client.servicecontrol;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ServiceControlAsync {
    void startService(AsyncCallback async);

    void pauseService(AsyncCallback async);

    void stopService(AsyncCallback async);

    void getServiceStatus(AsyncCallback async);
}
