package org.jboss.errai.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Timer;
import org.jboss.errai.client.util.effectimpl.MozillaEffectImpl;

public class Effects {
    /**
     * Load the browser-specific effect implementation.
     */
    private static Effect effect = GWT.create(Effect.class);
    static {
//        if (!GWT.isScript() && effect instanceof MozillaEffectImpl) {
//            effect = new Effect() {
//                public Timer doFade(Element el, double duration, int start, int end) {
//                    setOpacity(el, end);
//                    return null;
//                }
//
//                public void setOpacity(Element el, int opacity) {
//                    MozillaEffectImpl.setOpacityNative(el.getStyle(), opacity);
//                }
//            };
//        }
    }

    public static void fade(Element el, final double duration, final int start, final int end) {
        effect.doFade(el, duration, start, end);
    }

    public static void setOpacity(Element el, int opacity) {
        effect.setOpacity(el, opacity);
    }
}                                                                                             
