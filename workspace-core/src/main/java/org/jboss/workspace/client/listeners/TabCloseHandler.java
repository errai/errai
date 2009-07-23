package org.jboss.workspace.client.listeners;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import org.jboss.workspace.client.widgets.WSTab;
import org.jboss.workspace.client.widgets.WSModalDialog;
import org.jboss.workspace.client.layout.WorkspaceLayout;
import org.jboss.workspace.client.framework.AcceptsCallback;
import org.jboss.workspace.client.rpc.StatePacket;


public class TabCloseHandler implements ClickHandler, AcceptsCallback {
    /**
     * The reference to the tab.
     */
    private WSTab tab;
    private StatePacket packet;

    /**
     * The reference to the root GuvnorLayout object.
     */
    private WorkspaceLayout guvWorkspace;

    public TabCloseHandler(WSTab tab, WorkspaceLayout guvWorkspace, StatePacket packet) {
        this.tab = tab;
        this.guvWorkspace = guvWorkspace;
        this.packet = packet;
    }

    public void onClick(ClickEvent event) {
        /**
         * Check to see if the current tool has a modified flag.
         */
        if (tab.isModified()) {

            /**
             * Create a new warning Dialog
             */
            WSModalDialog dialog = new WSModalDialog();

            dialog.getOkButton().setText("Close Anyways");
            dialog.getCancelButton().setText("Don't Close");

            /**
             * Initialize the dialog
             */
            dialog.ask(
                    "You have unsaved changes, closing this dialog" +
                            " without saving will cause you lose date.",
                    this);



            /**
             * Prompt the user.
             */
            dialog.showModal();
        }
        else {
            close();
        }
    }

    /**
     * The callback receiver method for the warning dialog box.
     * @param message
     */
    public void callback(String message) {
        /**
         * If the user pressed okay, close the tab.
         */
        if (AcceptsCallback.MESSAGE_OK.equals(message)) {
            close();
        }
        else {
            /**
             * Do nothing.
             */
        }
    }

    /**
     * Close the tab.
     */
    public void close() {
        guvWorkspace.closeTab(packet);
    }
}
