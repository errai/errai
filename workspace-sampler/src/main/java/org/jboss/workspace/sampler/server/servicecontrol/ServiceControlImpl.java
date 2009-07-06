package org.jboss.workspace.sampler.server.servicecontrol;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.jboss.workspace.sampler.client.servicecontrol.ServiceControl;

public class ServiceControlImpl extends RemoteServiceServlet implements ServiceControl {
    private int status;

    public void startService() {
        System.out.println("Starting Service!");

        try {
            Thread.sleep(2000);
        }
        catch (Throwable t) {
        }


        status = STARTED;
    }

    public void pauseService() {
        System.out.println("Pausing Service!");

        try {
            Thread.sleep(1000);
        }
        catch (Throwable t) {
        }

        status = PAUSED;
    }

    public void stopService() {
        System.out.println("Stopping Service!");

        try {
            Thread.sleep(3000);
        }
        catch (Throwable t) {
        }
        status = STOPPED;

    }

    public int getServiceStatus() {
        return status;
    }

    public String getName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
