package org.jboss.errai.client.util.effectimpl;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Timer;
import org.jboss.errai.client.util.Effect;

public class KHTMLEffectImpl implements Effect {
    public Timer doFade(final Element el, double duration, final int start, final int end) {
        //   final Style s = el.getStyle();
        Timer t = start < end ?
                new Timer() {
                    int step = start;

                    public void run() {
                        step += 5;
                        if (step < end) {
                            _setOpacity(el, step);
                        }
                        else {
                            _setOpacity(el, end);
                            cancel();
                        }
                    }
                }
                :
                new Timer() {
                    int step = end;

                    public void run() {
                        step -= 5;
                        if (step > end) {
                            _setOpacity(el, step);
                        }
                        else {
                            _setOpacity(el, end);
                            cancel();
                        }
                    }
                };

        t.scheduleRepeating(1);

        return t;
    }

    public void setOpacity(Element el, int opacity) {
        _setOpacity(el, opacity);
    }

    public static void _setOpacity(Element el, float opacity) {
        el.getStyle().setProperty("opacity", String.valueOf(opacity / 100));
    }

}
