package org.jboss.workspace.client;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.core.client.EntryPoint;
import static com.google.gwt.core.client.GWT.create;
import com.google.gwt.user.client.Window;
import static com.google.gwt.user.client.Window.enableScrolling;
import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.workspace.client.framework.ModuleLoaderBootstrap;
import org.jboss.workspace.client.layout.WorkspaceLayout;
import org.jboss.workspace.client.rpc.StatePacket;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Workspace implements EntryPoint {
    public static PickupDragController dragController;
    private static WorkspaceLayout workspaceLayout;

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        init("rootPanel");
    }
                     
    public WorkspaceLayout init(String rootId) {
        if (workspaceLayout != null) {
            Window.alert("Workspace already initialized.");
            return null;
        }

        workspaceLayout = new WorkspaceLayout();
        workspaceLayout.setRpcSync(true);

        enableScrolling(false);


        if (rootId != null) {
            RootPanel.get(rootId).add(workspaceLayout.createLayout(rootId));
        }
        else {
            Window.alert("No root ID specified!");
            return null;
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

    public static void addToolSet(ToolSet toolSet) {
        workspaceLayout.addToolSet(toolSet);
    }

    public static void notifyState(StatePacket packet) {
        workspaceLayout.notifySessionState(packet);
    }

    public static WorkspaceLayout currentWorkspace() {
        return workspaceLayout;
    }
}
