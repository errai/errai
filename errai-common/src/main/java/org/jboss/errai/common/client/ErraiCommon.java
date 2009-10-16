package org.jboss.errai.common.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Element;

public class ErraiCommon implements EntryPoint {
    public void onModuleLoad() {
    }

    public static void disableTextSelection(Element e, boolean disable) {
        disableTextSelectInternal(e, disable);
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
