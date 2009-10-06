package org.jboss.errai.widgets.client.util;

import com.google.gwt.dom.client.Element;


public class LayoutUtil {


    public static void disableTextSelection(Element elem, boolean disable) {
        disableTextSelectInternal(elem, disable);
    }


    private native static void disableTextSelectInternal(Element e, boolean disable)/*-{
      if (disable) {
        e.ondrag = function () { return false; };
        e.onselectstart = function () { return false; };
      } else {
        e.ondrag = null;
        e.onselectstart = null;
      }
    }-*/;
}
