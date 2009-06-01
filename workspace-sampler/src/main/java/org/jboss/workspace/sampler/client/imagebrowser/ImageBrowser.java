package org.jboss.workspace.sampler.client.imagebrowser;

import org.jboss.workspace.client.framework.Tool;
import org.jboss.workspace.client.rpc.StatePacket;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.core.client.GWT;

/**
 * Created by IntelliJ IDEA.
 * User: christopherbrock
 * Date: 1-Jun-2009
 * Time: 3:25:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImageBrowser implements Tool {
    public Widget getWidget(StatePacket packet) {
        return new HTML("Image Browser");
    }

    public String getName() {
        return "Image Browser";
    }

    public String getId() {
        return "imageBrowser";
    }

    public Image getIcon() {
        return new Image(GWT.getModuleBaseURL() + "/images/ui/icons/camera_go.png");
    }

    public boolean multipleAllowed() {
        return true;
    }
}
