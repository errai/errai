package org.jboss.errai.client.util.effectimpl;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Timer;
import org.jboss.errai.client.util.Effect;

public class KHTMLEffectImpl implements Effect {
    public Timer doFade(final Element el, int durationMillis, final int stepping, final int start, final int end) {
     //   final Style s = el.getStyle();
        Timer t = start < end ?
                new Timer() {
                    int step = start;

                    public void run() {
                        step += stepping;
                        if (step < end) {
                            setOpacity(el, step);
                        }
                        else {
                            setOpacity(el, end);
                            cancel();
                        }
                    }
                }
                :
                new Timer() {
                    int step = end;

                    public void run() {
                        step -= stepping;
                        if (step > end) {
                            setOpacity(el, step);
                        }
                        else {
                            setOpacity(el, end);
                            cancel();
                        }
                    }
                };

        t.scheduleRepeating(durationMillis);

        return t;
    }

    public void setOpacity(Element el, int opacity) {
         setOpacityNative(el.getStyle(), opacity);
    }

    public native static void setOpacityNative(Style s, int opacity) /*-{
        s.opacity = opacity / 100;
    }-*/;
}
