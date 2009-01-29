package org.jboss.workspace.client.rpc.adapters;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.workspace.client.Workspace;
import org.jboss.workspace.client.rpc.Attachable;
import org.jboss.workspace.client.rpc.StatePacket;

public class TextAreaAttach implements Attachable {
    public void attach(final String id, final Widget widget, final StatePacket packet) {
        final TextArea w = (TextArea) widget;

        w.addChangeListener(
                new ChangeListener() {
                    public void onChange(Widget widget) {
                        packet.setParameter(id, w.getText());
                        packet.notifySessionState();
                    }
                }
        );

        String val = packet.getParameter(id);
        if (val != null) {
            w.setText(val);
        }
    }
}
