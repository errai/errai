package org.jboss.workspace.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.Window;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import org.jboss.workspace.client.layout.WorkspaceLayout;
import org.jboss.workspace.client.framework.ModuleLoaderBootstrap;
import org.jboss.workspace.client.rpc.StatePacket;
import org.gwt.mosaic.ui.client.Viewport;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Workspace implements EntryPoint {
  //  public static final WorkspaceLayout WORKSPACE = new WorkspaceLayout();
    public static PickupDragController dragController;

    private WorkspaceLayout workspaceLayout;

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        init(null);
    }

    public WorkspaceLayout init(String rootId) {

        workspaceLayout = new WorkspaceLayout();

        Window.enableScrolling(false);

        final Viewport viewport = new Viewport();
        viewport.add(workspaceLayout.createLayout());

        //  RootPanel.get().add(viewport);

        if (rootId != null) RootPanel.get(rootId).add(viewport);
        else RootPanel.get().add(viewport);

        dragController = new PickupDragController(RootPanel.get(), true);

        ModuleLoaderBootstrap mlb = GWT.create(ModuleLoaderBootstrap.class);
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

    public WorkspaceLayout getWorkspaceLayout() {
        return workspaceLayout;
    }
}
