package org.jboss.errai.widgets.rebind;

import org.jboss.errai.widgets.client.WSGrid;
import org.jboss.errai.widgets.rebind.collectionmappers.WSGridFMGenerator;

import java.util.HashMap;
import java.util.Map;

public class FieldMapperGeneratorFactory {
    private static final Map<String, FieldMapperGenerator> FIELD_MAPPERS =
            new HashMap<String, FieldMapperGenerator>();

    public static void addFieldMapper(String widgetType, FieldMapperGenerator mapper) {
        FIELD_MAPPERS.put(widgetType, mapper);
    }

    public static FieldMapperGenerator getFieldMapper(String widgetType) {
        return FIELD_MAPPERS.get(widgetType);
    }

    static {
        FIELD_MAPPERS.put(WSGrid.class.getName(), new WSGridFMGenerator());
    }
}
