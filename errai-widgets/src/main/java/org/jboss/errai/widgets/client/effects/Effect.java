package org.jboss.errai.widgets.client.effects;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Timer;

public interface Effect {
    public Timer doFade(Element el, double duration, final int start, final int end);

    public void setOpacity(Element el, int opacity);

}

