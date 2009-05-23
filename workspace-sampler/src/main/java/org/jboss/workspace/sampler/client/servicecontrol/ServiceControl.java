package org.jboss.workspace.sampler.client.servicecontrol;

import com.google.gwt.user.client.rpc.RemoteService;

public interface ServiceControl extends RemoteService {
    public static final int STOPPED = -1;
    public static final int PAUSED = 0;
    public static final int STARTED = 1;

    public void startService();
    public void pauseService();
    public void stopService();

    public int getServiceStatus();
}
