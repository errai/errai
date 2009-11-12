package org.jboss.errai.widgets.rebind;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

public interface FieldMapperGenerator {
    public String generateFieldMapperGenerator(
            TypeOracle oracle,
            String targetWidget,
            String targetType,
            String targetFieldType,
            String fieldName);

    public String generateValueExtractorStatement(
            TypeOracle oracle,
            String targetWidget,
            String targetType,
            String targetFieldType,
            String fieldName);

    public String init(
            TypeOracle oracle,
            String targetWidget,
            String targetType,
            String variable,
            List<JField> fields);

}
