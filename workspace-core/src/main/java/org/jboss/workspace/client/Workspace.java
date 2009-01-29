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
    public static final WorkspaceLayout WORKSPACE = new WorkspaceLayout();
    public static PickupDragController dragController;

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        init(null);
    }

    public WorkspaceLayout init(String rootId) {
        Window.enableScrolling(false);

        final Viewport viewport = new Viewport();
        viewport.add(WORKSPACE.createLayout());

        //  RootPanel.get().add(viewport);

        if (rootId != null) RootPanel.get(rootId).add(viewport);
        else RootPanel.get().add(viewport);

        dragController = new PickupDragController(RootPanel.get(), true);

        ModuleLoaderBootstrap mlb = GWT.create(ModuleLoaderBootstrap.class);
        mlb.initAll(WORKSPACE);

        /**
         * Get any session state information from the server.
         */
        WORKSPACE.pullSessionState();

        return WORKSPACE;
    }


    public static void addToolSet(ToolSet toolSet) {
        WORKSPACE.addToolSet(toolSet);
    }

    public static void notifyState(StatePacket packet) {
        WORKSPACE.notifySessionState(packet);
    }

    public WorkspaceLayout getWorkspaceLayout() {
        return WORKSPACE;
    }
}
