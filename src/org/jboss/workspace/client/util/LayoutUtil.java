package org.jboss.workspace.client.util;

import org.gwt.mosaic.ui.client.layout.BorderLayoutData;
import org.gwt.mosaic.ui.client.layout.BorderLayout;

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
}
