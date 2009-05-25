package org.jboss.workspace.client;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.core.client.EntryPoint;
import static com.google.gwt.core.client.GWT.create;
import static com.google.gwt.user.client.Window.enableScrolling;
import com.google.gwt.user.client.ui.RootPanel;
import org.gwt.mosaic.ui.client.Viewport;
import org.jboss.workspace.client.framework.ModuleLoaderBootstrap;
import org.jboss.workspace.client.layout.WorkspaceLayout;
import org.jboss.workspace.client.rpc.StatePacket;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Workspace implements EntryPoint {
    public static PickupDragController dragController;
    private WorkspaceLayout workspaceLayout;

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
    }
                     
    public WorkspaceLayout init(String rootId) {
        
        workspaceLayout = new WorkspaceLayout();


        enableScrolling(false);

        final Viewport viewport = new Viewport();
        viewport.add(workspaceLayout.createLayout());

        if (rootId != null) {
            RootPanel.get(rootId).add(viewport);
        }
        else {
            RootPanel.get().add(viewport);
        }

        dragController = new PickupDragController(RootPanel.get(), true);

        ModuleLoaderBootstrap mlb = create(ModuleLoaderBootstrap.class);
        mlb.initAll(workspaceLayout);

        /**
         * Get any session state information from the server.
         */
        workspaceLayout.pullSessionState();

        return workspaceLayout;
    }

    public void addToolSet(ToolSet toolSet) {
        workspaceLayout.addToolSet(toolSet);
    }

    public void notifyState(StatePacket packet) {
        workspaceLayout.notifySessionState(packet);
    }
}
