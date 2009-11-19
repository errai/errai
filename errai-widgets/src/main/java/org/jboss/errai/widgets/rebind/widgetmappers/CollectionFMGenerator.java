package org.jboss.errai.widgets.rebind.widgetmappers;

import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import org.jboss.errai.widgets.rebind.FieldMapperGenerator;

import java.util.List;

public class CollectionFMGenerator implements FieldMapperGenerator {
    public String generateFieldMapperGenerator(TypeOracle oracle, JField targetWidgetField, JType targetType, JField targetEntityField, String fieldName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String generateValueExtractorStatement(TypeOracle oracle, JField targetWidgetField, JType targetType, JField targetEntityField, String fieldName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String init(TypeOracle oracle, JField targetWidgetField, JType targetType, String variable, List<JField> fields) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
