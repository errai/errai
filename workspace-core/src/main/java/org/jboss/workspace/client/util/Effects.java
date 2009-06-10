package org.jboss.workspace.client.util;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Timer;

public class Effects {

    public static void fade(Element el, int durationMillis, final int stepping, final int start, final int end) {
        final Style s = el.getStyle();
        Timer t = start < end ?
                new Timer() {
                    int step = start;

                    public void run() {
                        step += stepping;
                        if (step < end) {
                            setOpacity(s, step);
                        }
                        else {
                            setOpacity(s, end);
                            cancel();
                        }
                    }
                }
                :
                new Timer() {
                    int step = end;

                    public void run() {
                        step -= stepping;
                        if (step < end) {
                            setOpacity(s, step);
                        }
                        else {
                            setOpacity(s, end);
                            cancel();
                        }
                    }
                };

        t.scheduleRepeating(durationMillis);
    }

    public native static void setOpacity(Style s, int opacity) /*-{
        s.opacity = opacity / 100;
//        s.MozOpacity = opacity / 100;
//        s.KthmlOpacity = opacity / 100;
        s.filter = "alpha(" + opacity + ")";
    }-*/;
}
