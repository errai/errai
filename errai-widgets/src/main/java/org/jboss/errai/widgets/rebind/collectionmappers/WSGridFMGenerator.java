/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.widgets.rebind.collectionmappers;

import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JType;
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
    public String generateFieldMapperGenerator(TypeOracle typeOracle, JField targetWidget, JType targetType, 
                                               JField targetEntityMember, JField targetEntityField) {
        InputStream istream = this.getClass().getResourceAsStream("WSGridFieldMappers.mv");
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("typeOracle", typeOracle);
        vars.put("targetWidget", targetWidget.getType().isClassOrInterface().getQualifiedSourceName());
        vars.put("targetType", targetType);
        vars.put("targetTypeName", targetType.isClassOrInterface().getQualifiedSourceName());
        vars.put("fieldName", targetEntityField.getName());

        return (String) TemplateRuntime.eval(istream, null, new MapVariableResolverFactory(vars), null);
    }

    public String generateValueExtractorStatement(TypeOracle oracle, JField targetWidget, JType targetType,
                                                  JField targetEntityMember, JField targetEntityField) {
        return "";
    }

    public String init(TypeOracle oracle, JField targetWidget, JType targetType, JField targetFieldType,
                       JField targetEntityField, String variable, List<JField> fields) {
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
