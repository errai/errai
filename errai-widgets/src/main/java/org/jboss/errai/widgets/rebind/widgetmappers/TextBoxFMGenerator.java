package org.jboss.errai.widgets.rebind.widgetmappers;

import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import org.jboss.errai.widgets.rebind.FieldMapperGenerator;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import org.mvel2.templates.TemplateRuntime;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextBoxFMGenerator implements FieldMapperGenerator {
    public String generateFieldMapperGenerator(TypeOracle oracle, String targetWidget, String targetType, String fieldName) {
        InputStream istream = this.getClass().getResourceAsStream("TextBoxFMGenerator.mv");
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("typeOracle", oracle);
        vars.put("targetWidget", targetWidget);
        vars.put("targetType", targetType);
        vars.put("fieldName", fieldName);

        return (String) TemplateRuntime.eval(istream, null, new MapVariableResolverFactory(vars), null);
    }

    public String init(TypeOracle oracle, String targetWidget, String targetType, String variable, List<JField> fields) {
        return "";
    }
}
