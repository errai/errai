package org.jboss.workspace.client.rpc.adapters;

import org.jboss.workspace.client.rpc.Attachable;
import org.jboss.workspace.client.rpc.StatePacket;
import org.jboss.workspace.client.Workspace;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.TextBox;

public class TextBoxAttach implements Attachable {
    public void attach(final String id, final Widget widget, final StatePacket packet) {
        final TextBox w = (TextBox) widget;

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