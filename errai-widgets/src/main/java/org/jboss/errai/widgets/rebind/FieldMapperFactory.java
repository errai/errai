package org.jboss.errai.widgets.rebind;

import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import org.jboss.errai.widgets.client.WSGrid;
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
        FIELD_MAPPERS.put(WSGrid.class.getName(), new FieldMapperGenerator() {
            public String generateFieldMapperGenerator(TypeOracle typeOracle, String targetWidget, String targetType, String fieldName) {
                InputStream istream = this.getClass().getResourceAsStream("WSGridFieldMappers.mv");
                Map<String, Object> vars = new HashMap<String, Object>();
                vars.put("typeOracle", typeOracle);
                vars.put("targetWidget", targetWidget);
                vars.put("targetType", targetType);
                vars.put("fieldName", fieldName);

                return (String) TemplateRuntime.eval(istream, null, new MapVariableResolverFactory(vars), null);
            }

            public String init(TypeOracle oracle, String targetWidget, String targetType, String variable, List<JField> fields) {
                StringBuilder builder = new StringBuilder(variable + ".setDefaultTitleValues(new String[] {");

                Iterator<JField> iter = fields.iterator();
                while (iter.hasNext()) {
                    builder.append("\"").append(iter.next().getName()).append("\"");
                    if (iter.hasNext()) builder.append(", ");
                }
                return builder.append("});").toString();
            }
        }
        );
    }
}
