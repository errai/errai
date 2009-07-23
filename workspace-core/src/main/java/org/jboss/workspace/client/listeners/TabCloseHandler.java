package org.jboss.workspace.client.listeners;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import org.jboss.workspace.client.framework.AcceptsCallback;
import org.jboss.workspace.client.rpc.StatePacket;
import org.jboss.workspace.client.widgets.WSModalDialog;


public class TabCloseHandler implements CloseHandler, AcceptsCallback {
    /**
     * The reference to the tab.
     */
    private StatePacket packet;

    public TabCloseHandler(StatePacket packet) {
        this.packet = packet;
    }


    public void onClose(CloseEvent closeEvent) {
        /**
         * Check to see if the current tool has a modified flag.
         */
        if (packet.getTabInstance().isModified()) {

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
            packet.deactive();
        }
    }


    /**
     * The callback receiver method for the warning dialog box.
     *
     * @param message
     */
    public void callback(String message) {
        /**
         * If the user pressed okay, close the tab.
         */
        if (AcceptsCallback.MESSAGE_OK.equals(message)) {
             packet.deactive();
        }
        else {
            /**
             * Do nothing.
             */
        }
    }


}
