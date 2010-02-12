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
import org.jboss.errai.widgets.rebind.FieldMapperGenerator;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionFMGenerator implements FieldMapperGenerator {
    CompiledTemplate compiledTemplate;

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
            throw new RuntimeException("cannot generateGetField mappers for collection of widgets (the collection is not properly parameterized: eg. List<Widget>)");
        }

        JClassType widgetType = paramaterizedType.getTypeArgs()[0];
        varName = targetEntityField.getType().isClassOrInterface().getName() + targetWidgetField.getName() + "Mapper";

        if (compiledTemplate == null) {
            InputStream istream = this.getClass().getResourceAsStream("CollectionFMGenerator.mv");
            compiledTemplate = TemplateCompiler.compileTemplate(istream, null);
        }

        Map vars = new HashMap();
        vars.put("typeOracle", oracle);
        vars.put("targetWidgetField", targetWidgetField);
        vars.put("targetEntityField", targetEntityField);
        vars.put("targetEntityMember", targetEntityMember);
        vars.put("widgetType", widgetType);
        vars.put("entityCollectionType", entityCollectionType);
        vars.put("widgetCollectionType", widgetCollectionType);
        vars.put("varName", varName);
      
        return String.valueOf(TemplateRuntime.execute(compiledTemplate, vars));
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
