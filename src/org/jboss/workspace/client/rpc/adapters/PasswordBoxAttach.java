package org.jboss.workspace.client.rpc.adapters;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.PasswordTextBox;
import org.jboss.workspace.client.Workspace;
import org.jboss.workspace.client.rpc.Attachable;
import org.jboss.workspace.client.rpc.StatePacket;

public class PasswordBoxAttach implements Attachable {
    public void attach(final String id, final Widget widget, final StatePacket packet) {
        final PasswordTextBox w = (PasswordTextBox) widget;

        w.addChangeListener(
                new ChangeListener() {
                    public void onChange(Widget widget) {
                        packet.setParameter(id, w.getText());
                        Workspace.notifyState(packet);
                    }
                }
        );

        String val = packet.getParameter(id);
        if (val != null) {
            w.setText(val);
        }
    }
}