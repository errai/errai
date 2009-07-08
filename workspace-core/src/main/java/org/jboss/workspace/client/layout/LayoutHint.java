package org.jboss.workspace.client.layout;

import com.google.gwt.user.client.ui.Widget;

import java.util.HashMap;

public class LayoutHint {
    private static HashMap<Widget, LayoutHintProvider> MANGED_WIDGETS = new HashMap<Widget, LayoutHintProvider>();

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
//        Widget w = instance;
//        while (w != null) {
//    //        System.out.println(">" + w.getClass().getName() + " (" + (w instanceof LayoutHintManager ? "YES": "NO") + ")");
//            if (w instanceof LayoutHintManager && ((LayoutHintManager) w).isManaged(w)) {
//      //          System.out.println("found!");
//                return ((LayoutHintManager) w).getProvider(instance);
//            }
//
//            w = w.getParent();
//        }
//        return null;
    }

    public static void hintAll() {
        LayoutHintProvider p;

        for (Widget w : MANGED_WIDGETS.keySet()) {
            p = findProvider(w);

            if (p != null) {
                w.setPixelSize(p.getWidthHint(), p.getHeightHint());
            }
        }
    }
}
