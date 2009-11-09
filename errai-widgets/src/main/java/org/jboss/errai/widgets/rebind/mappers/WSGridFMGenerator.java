package org.jboss.errai.widgets.rebind.mappers;

import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import org.jboss.errai.widgets.rebind.FieldMapperGenerator;
import org.jboss.errai.widgets.rebind.FriendlyName;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import org.mvel2.templates.TemplateRuntime;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WSGridFMGenerator implements FieldMapperGenerator {
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
        JField fld;
        String fieldName;
        while (iter.hasNext()) {
            if ((fld = iter.next()).isAnnotationPresent(FriendlyName.class)) {
                fieldName = fld.getAnnotation(FriendlyName.class).value();
            } else {
                fieldName = fld.getName();
            }

            builder.append("\"").append(fieldName).append("\"");
            if (iter.hasNext()) builder.append(", ");
        }
        return builder.append("});").toString();
    }
}
