package org.jboss.workspace.sampler.client.filebrowser;

import org.jboss.workspace.client.framework.Tool;
import org.jboss.workspace.client.rpc.StatePacket;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.core.client.GWT;


public class FileBrowserTool  implements Tool {
    public Widget getWidget() {
        return new FileBrowserWidget();
    }

    public String getName() {
        return "File Browser";
    }

    public String getId() {
        return "filebrowser";
    }

    public Image getIcon() {
        return new Image(GWT.getModuleBaseURL() + "/images/ui/icons/table_multiple.png");
    }

    public boolean multipleAllowed() {
        return false;
    }
}