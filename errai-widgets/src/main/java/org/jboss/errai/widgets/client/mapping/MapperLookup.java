package org.jboss.errai.widgets.client.mapping;

import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.widgets.client.WSGrid;
import org.jboss.errai.widgets.client.mapping.collectionimpl.WSGridMapper;

import java.util.HashMap;
import java.util.Map;

public class MapperLookup {
    private static final Map<String, Class<? extends WidgetMapper>> WIDGET_TO_MAPPER
            = new HashMap<String, Class<? extends WidgetMapper>>();

    static {
        WIDGET_TO_MAPPER.put(WSGrid.class.getName(), WSGridMapper.class);
      //  WIDGET_TO_MAPPER.put(TextBox.class.getName(), TextBoxMapper.class);

    }

    public static Class<? extends CollectionWidgetMapper> lookupCollectionMapper(String widgetType) {
        return (Class<? extends CollectionWidgetMapper>) WIDGET_TO_MAPPER.get(widgetType);
    }

    public static Class<? extends WidgetMapper> lookupWidgetMapper(String widgetType) {
        return WIDGET_TO_MAPPER.get(widgetType);
    }
}
