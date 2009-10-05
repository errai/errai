package org.jboss.errai.client.layout;

import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.client.bus.CommandMessage;
import org.jboss.errai.client.bus.MessageBusClient;
import org.jboss.errai.client.framework.MessageCallback;
import static org.jboss.errai.client.bus.MessageBusClient.subscribe;
import org.jboss.errai.client.bus.protocols.LayoutParts;

import java.util.LinkedHashMap;

public class LayoutHint {
    private static LinkedHashMap<Widget, LayoutHintProvider> MANAGED_WIDGETS = new LinkedHashMap<Widget, LayoutHintProvider>();
    private static LinkedHashMap<String, LayoutHintProvider> MANAGED_SUBJECTS = new LinkedHashMap<String, LayoutHintProvider>();

    private static int counter = 0;

    public static void attach(final Widget w, LayoutHintProvider p) {
        String subject = "local:org.jboss.errai.sizeHints:" + counter++;

        subscribe(subject,
                new MessageCallback() {
                    public void callback(CommandMessage message) {
                        w.setPixelSize(message.get(Double.class, LayoutParts.Width).intValue(),
                                message.get(Double.class, LayoutParts.Height).intValue());
                    }
                }, null);


        MANAGED_WIDGETS.put(w, p);
        MANAGED_SUBJECTS.put(subject, p);
    }

    public static LayoutHintProvider findProvider(Widget instance) {
        return MANAGED_WIDGETS.get(instance);
    }

    public static LayoutHintProvider findProvider(String subject) {
        return MANAGED_SUBJECTS.get(subject);
    }

    public static void hintAll() {
        LayoutHintProvider p;
        for (String s : MANAGED_SUBJECTS.keySet()) {
            if ((p = findProvider(s)) != null && p.getWidthHint() > 0 && p.getHeightHint() > 0) {
                MessageBusClient.send(s, CommandMessage.create()
                        .set(LayoutParts.Width, p.getWidthHint())
                        .set(LayoutParts.Height, p.getHeightHint()));
            }
        }

        for (Widget w : MANAGED_WIDGETS.keySet()) {
            p = findProvider(w);
            if (p != null && w.isAttached() && p.getWidthHint() > 0 && p.getHeightHint() > 0) {
                w.setPixelSize(p.getWidthHint(), p.getHeightHint());
            }
        }
    }
}
