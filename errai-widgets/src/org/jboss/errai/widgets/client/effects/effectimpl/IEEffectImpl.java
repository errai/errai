package org.jboss.errai.widgets.client.effects.effectimpl;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Timer;
import org.jboss.errai.widgets.client.effects.Effect;


public class IEEffectImpl implements Effect {
    public Timer doFade(final Element el, double duration ,final int start, final int end) {
        Timer t = start < end ?
                new Timer() {
                    int step = start;

                    public void run() {
                        step +=  5;
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
                        step -=  5;
                        if (step > end) {
                            setOpacity(el, step);
                        }
                        else {
                            setOpacity(el, end);
                            cancel();
                        }
                    }
                };

        t.scheduleRepeating(1);

        return t;
    }

    public void setOpacity(Element el, int opacity) {
        el.getStyle().setProperty("filter", "progid:DXImageTransform.Microsoft.Alpha(opacity='" + opacity + "')");        
    }
}
