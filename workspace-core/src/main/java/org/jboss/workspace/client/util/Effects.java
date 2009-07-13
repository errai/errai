package org.jboss.workspace.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import org.jboss.workspace.client.util.effectimpl.MozillaEffectImpl;

public class Effects {
    /**
     * Load the browser-specific effect implementation.
     */
    private static Effect effect = GWT.create(Effect.class);
    static {
        if (!GWT.isScript() && effect instanceof MozillaEffectImpl) {
            effect = new Effect() {
                public void doFade(Element el, int durationMillis, int stepping, int start, int end) {
                    setOpacity(el, end);
                }

                public void setOpacity(Element el, int opacity) {
                    MozillaEffectImpl.setOpacityNative(el.getStyle(), opacity);
                }
            };
        }
    }

    public static void fade(Element el, int durationMillis, final int stepping, final int start, final int end) {
        effect.doFade(el, durationMillis, stepping, start, end);
    }

    public static void setOpacity(Element el, int opacity) {
        effect.setOpacity(el, opacity);
    }
}
