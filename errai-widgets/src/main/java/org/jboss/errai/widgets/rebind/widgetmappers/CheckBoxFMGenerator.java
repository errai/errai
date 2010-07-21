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

package org.jboss.errai.widgets.rebind.widgetmappers;

import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import org.jboss.errai.widgets.rebind.FieldMapperGenerator;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import org.mvel2.templates.TemplateRuntime;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckBoxFMGenerator implements FieldMapperGenerator {
    public String generateFieldMapperGenerator(TypeOracle oracle, JField targetWidget,
                                               JType targetType, JField targeEntityMember, JField targetEntityField) {
        InputStream istream = this.getClass().getResourceAsStream("CheckBoxFMGenerator.mv");
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("typeOracle", oracle);
        vars.put("targetWidget", targetWidget);
        vars.put("targetType", targetType);
        vars.put("fieldType", targeEntityMember);
        vars.put("fieldName", targetEntityField.getName());

        return (String) TemplateRuntime.eval(istream, null, new MapVariableResolverFactory(vars), null);
    }

    public String generateValueExtractorStatement(TypeOracle oracle, JField targetWidget,
                                                  JType targetType, JField targetEntityMember, JField targetEntityField) {
        return "getText()";
    }

    public String init(TypeOracle oracle, JField targetWidget, JType targetType, JField targetFieldType, JField targetEntityField,
                       String variable, List<JField> fields) {
        return "";
    }
}