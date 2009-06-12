package org.jboss.workspace.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;

public class Effects {
    /**
     * Load the browser-specific effect implementation.
     */
    private static Effect effect = GWT.create(Effect.class);

    public static void fade(Element el, int durationMillis, final int stepping, final int start, final int end) {
        effect.doFade(el, durationMillis, stepping, start, end);
    }
    
    public static void setOpacity(Element el, int opacity) {
        effect.setOpacity(el, opacity);
    }
}
