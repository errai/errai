/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.widgets.rebind.widgetmappers;

import com.google.gwt.core.ext.typeinfo.*;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.widgets.client.mapping.FieldMapper;
import org.jboss.errai.widgets.rebind.FieldMapperGenerator;
import org.jboss.errai.widgets.rebind.FieldMapperGeneratorFactory;
import org.mvel2.util.ReflectionUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.jboss.errai.widgets.rebind.FieldMapperGeneratorFactory.getFieldMapper;
import static org.mvel2.util.ReflectionUtil.getGetter;

public class CollectionFMGenerator implements FieldMapperGenerator {
    String varName;

    public String generateFieldMapperGenerator(TypeOracle oracle, JField targetWidgetField, JType targetType, JField targetEntityMember, JField targetEntityField) {
        return varName;
    }

    public String generateValueExtractorStatement(TypeOracle oracle, JField targetWidgetField, JType targetType, JField targetEntityMember, JField targetEntityField) {
        return varName;
    }

    public String init(TypeOracle oracle, JField targetWidgetField, JType targetType, JField targetEntityMember, JField targetEntityField, String variable, List<JField> fields) {
        JClassType widgetCollectionType = targetWidgetField.getType().isClassOrInterface();
        JClassType entityCollectionType = targetEntityMember.getType().isClassOrInterface();

        JParameterizedType paramaterizedType = widgetCollectionType.isParameterized();

        if (paramaterizedType == null) {
            throw new RuntimeException("cannot generate mappers for collection of widgets (the collection is not properly parameterized: eg. List<Widget>)");
        }

        JClassType widgetType = paramaterizedType.getTypeArgs()[0];

        StringBuilder gen = new StringBuilder();

        gen.append("if (widget.").append(targetWidgetField.getName()).append(" == null) {\n");
        gen.append("   widget.").append(targetWidgetField.getName()).append(" = new ");
        if (getType(oracle, Set.class).isAssignableFrom(widgetCollectionType)) {
            gen.append(HashSet.class.getName());
        } else if (getType(oracle, List.class).isAssignableFrom(widgetCollectionType)) {
            gen.append(ArrayList.class.getName());
        }

        gen.append("();\n");

        gen.append("if (widget.").append(targetEntityField.getName()).append(" == null)").append(" {\n")
                .append("throw new RuntimeException(\"Target field '").append(targetEntityField.getName()).append("' is null\");\n");
        gen.append("}\n");

        gen.append("if (widget.").append(targetEntityField.getName()).append(".")
                .append(getGetter(targetEntityMember.getName())).append("() == null) {\n")
                .append("throw new RuntimeException(\"Target field '").append(targetEntityField.getName())
                .append(".").append(targetEntityMember.getName()).append("' is null\");\n");
        gen.append("}\n");

//        gen.append("for (Object o : widget.").append(targetEntityField.getName()).append(".")
//                .append(getGetter(targetEntityMember.getName()))
//                .append("()) {\n")
//                .append("widget.").append(targetWidgetField.getName()).append(".add(new ")
//                .append(widgetType.getQualifiedSourceName()).append("(String.valueOf(o)));\n");
//        gen.append("}\n");

        gen.append("}\n");

        varName = targetEntityField.getType().isClassOrInterface().getName() + targetWidgetField.getName() + "Mapper";
        gen.append("final ").append(FieldMapper.class.getName()).append(" ").append(varName).append(" = ");

        gen.append("new ").append(FieldMapper.class.getName()).append("<").append(widgetType.getQualifiedSourceName())
                .append(", ").append(widgetType.getQualifiedSourceName()).append(", ")
                .append(entityCollectionType.getQualifiedSourceName()).append(" >() {\n");
        gen.append("   public ").append(widgetType.getQualifiedSourceName()).append(" getFieldValue(")
                .append(widgetType.getQualifiedSourceName()).append(" w, ")
                .append(entityCollectionType.getQualifiedSourceName()).append(" value) {\n");
        gen.append("            for (").append(widgetType.getQualifiedSourceName()).append(" wid : widget.")
                .append(targetWidgetField.getName()).append(") {\n");

        if (getType(oracle, CheckBox.class).isAssignableFrom(widgetType)) {
            gen.append("        if (value.contains(wid.getText())) {\n");
            gen.append("            wid.setValue(true);\n");
            gen.append("        } else { \n");
            gen.append("            wid.setValue(false);\n");
            gen.append("        }\n");
        }

        gen.append("        }\n");
        gen.append("return w;\n");
        gen.append("     }\n");

        gen.append("    public void setFieldValue(").append(widgetType.getQualifiedSourceName()).append(" w, ")
                .append(entityCollectionType.getQualifiedSourceName()).append(" value) {\n");

        gen.append("        for (").append(widgetType.getQualifiedSourceName()).append(" wid : widget.")
                .append(targetWidgetField.getName()).append(") {\n");

        if (getType(oracle, CheckBox.class).isAssignableFrom(widgetType)) {
            gen.append("        if (wid.getValue()) {\n");
            gen.append("            value.add(wid.getText());\n");
            gen.append("        } else {\n");
            gen.append("            value.remove(wid.getText());\n");
            gen.append("        }");
        }

        gen.append("        }\n");

        gen.append("    }\n");
        gen.append("};");

        gen.append("for (final ").append(widgetType.getQualifiedSourceName()).append(" wid : widget.")
                .append(targetWidgetField.getName()).append(") {\n");

        if (getType(oracle, CheckBox.class).isAssignableFrom(widgetType)) {
            gen.append("wid.addValueChangeHandler(new " + ValueChangeHandler.class.getName() + "<Boolean>() {\n");
            gen.append("public void onValueChange(" + ValueChangeEvent.class.getName() + "<Boolean> booleanValueChangeEvent) {\n");
            gen.append(varName).append(".setFieldValue(wid, widget.").append(targetEntityField.getName()).append(".").append(getGetter(targetEntityMember.getName())).append("());\n");
            gen.append("}\n});");
        }

        gen.append("}");

        System.out.println(gen.toString());

        return gen.toString();
    }


    private JClassType getType(TypeOracle oracle, Class cls) {
        try {
            return oracle.getType(cls.getName());
        }
        catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
