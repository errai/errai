package org.jboss.errai.workspaces.client.util;

import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

public class LayoutUtil {
    public static void layoutHints(Iterable<Widget> widgets) {
        for (Widget w : widgets) {
            if (w instanceof RequiresResize) {
                ((RequiresResize)w).onResize();
            }
        }
    }
}
