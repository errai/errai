package org.jboss.workspace.client.util;

import com.google.gwt.dom.client.Style;

import static java.lang.String.valueOf;

public class Effects {

    public static void setOpacity(Style s, float factor) {
        String sf = valueOf(factor);

        s.setProperty("filter", "alpha(opacity=" + ((int) factor * 100) + ")");
        s.setProperty("-moz-opacity", sf);
        s.setProperty("-khtml-opacity", sf);
        s.setProperty("opacity", sf);
    }

}
