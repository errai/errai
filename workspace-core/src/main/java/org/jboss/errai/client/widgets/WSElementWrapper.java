package org.jboss.errai.client.widgets;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.Element;


public class WSElementWrapper extends Widget {
    public WSElementWrapper(Element e) {
        setElement(e);
    }

    @Override
    public void setPixelSize(int width, int height) {
        super.setPixelSize(width, height);
        getElement().getStyle().setProperty("width", width + "px");
        getElement().getStyle().setProperty("height", height + "px");
    }
}
