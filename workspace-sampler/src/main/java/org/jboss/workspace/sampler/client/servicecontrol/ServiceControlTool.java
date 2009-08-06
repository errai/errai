package org.jboss.workspace.sampler.client.servicecontrol;

import org.jboss.workspace.client.framework.Tool;
import org.jboss.workspace.client.rpc.StatePacket;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.core.client.GWT;

public class ServiceControlTool implements Tool {
    public Widget getWidget() {
        return new ServiceWidget();
    }

    public String getName() {
        return "Service Control";
    }

    public String getId() {
        return "serviceControl";
    }

    public Image getIcon() {
        return new Image(GWT.getModuleBaseURL() + "/images/ui/icons/server_go.png");
    }

    public boolean multipleAllowed() {
        return false;  
    }
}
