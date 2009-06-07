package org.jboss.workspace.client.util;

import com.google.gwt.dom.client.Element;
import org.gwt.mosaic.ui.client.layout.BorderLayout;
import org.gwt.mosaic.ui.client.layout.BorderLayoutData;

public class LayoutUtil {
    public static final int NORTH = 0;
    public static final int EAST = 1;
    public static final int SOUTH = 2;
    public static final int WEST = 3;
    public static final int CENTER = 4;

    public static BorderLayoutData position(int direction) {
        switch (direction) {
            case NORTH:
                return new BorderLayoutData(BorderLayout.Region.NORTH);
            case EAST:
                return new BorderLayoutData(BorderLayout.Region.EAST);
            case SOUTH:
                return new BorderLayoutData(BorderLayout.Region.SOUTH);
            case WEST:
                return new BorderLayoutData(BorderLayout.Region.WEST);
            case CENTER:
                return new BorderLayoutData(BorderLayout.Region.CENTER, true);
        }


        return null;
    }


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
