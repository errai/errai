package org.jboss.errai.workspaces.client.rpc.adapters;

import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.workspaces.client.rpc.Attachable;

public class TextAreaAttach implements Attachable {
    public void attach(final String id, final Widget widget, final String packet) {
        final TextArea w = (TextArea) widget;

//        w.addChangeListener(
//                new ChangeListener() {
//                    public void onChange(Widget widget) {
//                        packet.setParameter(id, w.getText());
//                        packet.notifySessionState();
//                    }
//                }
//        );
//
//        String val = packet.getParameter(id);
//        if (val != null) {
//            w.setText(val);
//        }
    }
}
