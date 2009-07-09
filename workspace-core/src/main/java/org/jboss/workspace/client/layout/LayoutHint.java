package org.jboss.workspace.client.layout;

import com.google.gwt.user.client.ui.Widget;

import java.util.LinkedHashMap;

public class LayoutHint {
    private static LinkedHashMap<Widget, LayoutHintProvider> MANGED_WIDGETS = new LinkedHashMap<Widget, LayoutHintProvider>();

    public static void attach(Widget w, LayoutHintProvider p) {
        MANGED_WIDGETS.put(w, p);
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
        return MANGED_WIDGETS.get(instance);
    }

    public static void hintAll() {
        LayoutHintProvider p;
        for (Widget w : MANGED_WIDGETS.keySet()) {
            p = findProvider(w);
            if (p != null && w.isAttached() && p.getWidthHint() > 0 && p.getHeightHint() > 0) {
                w.setPixelSize(p.getWidthHint(), p.getHeightHint());
            }
        }
    }
}
