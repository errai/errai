package org.jboss.errai.widgets.rebind;

import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import org.jboss.errai.widgets.client.WSGrid;
import org.jboss.errai.widgets.rebind.mappers.WSGridFMGenerator;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import org.mvel2.templates.TemplateRuntime;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FieldMapperFactory {
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
