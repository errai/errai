package org.jboss.workspace.client.util;

import com.google.gwt.dom.client.Style;

import static java.lang.String.valueOf;

public class Effects {

    public static void setOpacity(Style s, float factor) {
        String sf = valueOf(factor);

        s.setProperty("filter", "alpha(opacity=" + ((int) factor * 100) + ")");

        setOpacityDirect(s, "-moz-opacity", sf);
        setOpacityDirect(s, "-khtml-opacity", sf);
        setOpacityDirect(s, "opacity", sf);
    }

    static native void setOpacityDirect(Style style, String name, String opacity) /*-{
        style[name] = opacity;
    }-*/;

}
