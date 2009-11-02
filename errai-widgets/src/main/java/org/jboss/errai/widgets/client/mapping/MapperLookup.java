package org.jboss.errai.widgets.client.mapping;

import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.widgets.client.WSGrid;

import java.util.HashMap;
import java.util.Map;

public class MapperLookup {
    private static final Map<String, Class<? extends WidgetMapper>> WIDGET_TO_MAPPER
            = new HashMap<String, Class<? extends WidgetMapper>>();

    static {
        WIDGET_TO_MAPPER.put(WSGrid.class.getName(), WSGridMapper.class);
    }

    public static Class<? extends WidgetMapper> lookup(String widgetType) {
        return WIDGET_TO_MAPPER.get(widgetType);
    }
}
