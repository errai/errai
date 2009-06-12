package org.jboss.workspace.client.util;

import com.google.gwt.dom.client.Element;

public interface Effect {
    public void doFade(Element el, int durationMillis, final int stepping, final int start, final int end);

    public void setOpacity(Element el, int opacity);
}
