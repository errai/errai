package org.jboss.workspace.client.layout;

import com.google.gwt.user.client.ui.Widget;
import org.jboss.workspace.client.framework.CommandProcessor;
import org.jboss.workspace.client.rpc.MessageBusClient;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LayoutHint {
    private static LinkedHashMap<Widget, LayoutHintProvider> MANAGED_WIDGETS = new LinkedHashMap<Widget, LayoutHintProvider>();
    private static LinkedHashMap<String, LayoutHintProvider> MANAGED_SUBJECTS = new LinkedHashMap<String, LayoutHintProvider>();

    public static void attach(Widget w, LayoutHintProvider p) {
        MANAGED_WIDGETS.put(w, p);
    }

    public static void attach(String subject, LayoutHintProvider p) {
        MANAGED_SUBJECTS.put(subject, p);
    }


    public static int findHeightHint(Widget instance) {
        LayoutHintProvider p = findProvider(instance);
        return p == null ? 0 : p.getHeightHint();
    }

    public static int findWidthHint(Widget instance) {
        LayoutHintProvider p = findProvider(instance);
        return p == null ? 0 : p.getWidthHint();
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
            p = findProvider(s);
            if (p != null && p.getWidthHint() > 0 && p.getHeightHint() > 0) {
                Map<String, Object> msg = new HashMap<String, Object>();
                msg.put(CommandProcessor.MessageParts.Width.name(), p.getWidthHint());
                msg.put(CommandProcessor.MessageParts.Height.name(), p.getHeightHint());

                MessageBusClient.store(s, msg);

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
