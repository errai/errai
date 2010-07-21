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

package org.jboss.errai.widgets.rebind;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.*;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import org.jboss.errai.widgets.client.mapping.CollectionWidgetMapper;
import org.jboss.errai.widgets.client.mapping.ErraiWidgetBinding;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;

import static org.jboss.errai.widgets.rebind.FieldMapperGeneratorFactory.getFieldMapper;

public class WidgetMappingsGenerator extends Generator {
    /**
     * Simple name of class to be generated
     */
    private String className = null;

    private JClassType targetClass = null;

    /**
     * Package name of class to be generated
     */
    private String packageName = null;
    private String strTargetType;
    private String typeName;

    private TypeOracle typeOracle;

    private CompiledTemplate mappingsGen;
    private CompiledTemplate entityMappingGen;
    private CompiledTemplate addAllToGen;

    @Override
    public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
        typeOracle = context.getTypeOracle();
        this.typeName = typeName;

        InputStream istream = this.getClass().getResourceAsStream("CollectionMappingGenerator.mv");
        mappingsGen = TemplateCompiler.compileTemplate(istream, null);

        istream = this.getClass().getResourceAsStream("WidgetMappings.mv");
        entityMappingGen = TemplateCompiler.compileTemplate(istream, null);

        istream = this.getClass().getResourceAsStream("AddAllTo.mv");
        addAllToGen = TemplateCompiler.compileTemplate(istream, null);

        try {
            // get classType and save instance variables
            JClassType classType = typeOracle.getType(typeName);
            JClassType widgetBindingIface = typeOracle.getType(ErraiWidgetBinding.class.getName());

            for (JClassType t : classType.getImplementedInterfaces()) {
                if (widgetBindingIface.isAssignableFrom(t)) {
                    JParameterizedType pt = t.isParameterized();

                    if (pt != null) {
                        if (pt.getTypeArgs().length == 1) {
                            JClassType p = pt.getTypeArgs()[0];

                            String name = p.getName();
                            String pkg = p.getPackage().getName();
                            targetClass = typeOracle.getType(pkg + "." + name);
                        }
                    }
                }
            }

            if (targetClass == null) {
                throw new RuntimeException("Type " + ErraiWidgetBinding.class.getSimpleName()
                        + " must be bound via an extended interface with a type parameter " +
                        "(eg. interface WidgetBinding extends " + ErraiWidgetBinding.class.getSimpleName() + "<MyWidget> {} )");
            }

            packageName = classType.getPackage().getName();
            className = targetClass.getName().replaceAll("\\.", "_") + classType.getSimpleSourceName();
            strTargetType = targetClass.getPackage().getName() + "." + targetClass.getName();

            logger.log(TreeLogger.INFO, "Generating Extensions Bootstrapper...");

            // Generate class source code
            generateClass(logger, context);

        }
        catch (Throwable e) {

            // record sendNowWith logger that Map generation threw an exception
            logger.log(TreeLogger.ERROR, "Error generating extensions", e);
            e.printStackTrace();
        }

        // return the fully qualifed name of the class generated
        return packageName + "." + className;
    }

    private void generateClass(TreeLogger logger, GeneratorContext context) {
        // get print writer that receives the source code
        PrintWriter printWriter = context.tryCreate(logger, packageName, className);
        // print writer if null, source code has ALREADY been generated,

        if (printWriter == null) {
           return;
        }

        // init composer, set class properties, create source writer
        ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(packageName,
                className);
        composer.addImplementedInterface(typeName);
        composer.addImport(Widget.class.getName());
        


        SourceWriter sourceWriter = composer.createSourceWriter(context, printWriter);

        // generator constructor source code
        generateExtensions(context, logger, sourceWriter);
        // close generated class
        sourceWriter.outdent();
        sourceWriter.println("}");

        // commit generated class
        context.commit(logger, printWriter);
    }

    private void generateExtensions(GeneratorContext context, TreeLogger logger, SourceWriter sourceWriter) {
        // start constructor source generation
        sourceWriter.println("public " + className + "() { ");
        sourceWriter.println("}");

        sourceWriter.println("public void mapAll(final " + strTargetType + " widget) { ");
        sourceWriter.outdent();

        try {
            JClassType widgetMapper = typeOracle.getType(CollectionWidgetMapper.class.getName());

            for (JField currField : targetClass.getFields()) {
                if (currField.isAnnotationPresent(WidgetMapper.class) && widgetMapper.isAssignableFrom(currField.getType().isClassOrInterface())) {
                    WidgetMapper mf = currField.getAnnotation(WidgetMapper.class);

                    JField widgetField = targetClass.getField(mf.value());
                    String varName = widgetField.getName() + "Mapper";

                    JClassType binderField = currField.getType().isClassOrInterface();
                    JParameterizedType pType = binderField.isParameterized();

                    if (pType == null) {
                        RuntimeException e = new RuntimeException("Field '" + currField.getName() + "' must be parameterized");
                        logger.log(TreeLogger.Type.ERROR, e.getMessage(), e);
                        throw e;
                    }

                    // The last type arg shall always be our domain object type per this spec.
                    JClassType jEntityTarget = pType.getTypeArgs()[pType.getTypeArgs().length - 1];
                    String strTypeParms = generateTypeParmString(pType);

                    List<JField> fieldsToMap = new LinkedList<JField>();

                    /**
                     * If an EntityFields annotatio is present, then we discriminate on those fields.  Otherwise
                     * we capture all fields by default.
                     */
                    if (currField.isAnnotationPresent(EntityFields.class)) {
                        EntityFields ef = currField.getAnnotation(EntityFields.class);
                        for (String fieldName : ef.value()) {
                            JField fld = jEntityTarget.getField(fieldName);
                            if (fld == null) {
                                RuntimeException e = new RuntimeException("no such field in entity class '" + jEntityTarget.getName() + "': " + fieldName);
                                logger.log(TreeLogger.Type.ERROR, e.getMessage(), e);
                                throw e;
                            }

                            fieldsToMap.add(jEntityTarget.getField(fieldName));
                        }
                    } else {
                        for (JField fld : jEntityTarget.getFields()) {
                            if (fld.getEnclosingType().equals(jEntityTarget)) {
                                fieldsToMap.add(fld);
                            }
                        }
                    }

                    List<String> generatedInitializations = new LinkedList<String>();
                    List<String> generatedBindings = new LinkedList<String>();

                    FieldMapperGenerator g =
                            getFieldMapper(widgetField.getType().getQualifiedSourceName());

                    generatedInitializations.add(g.init(typeOracle, widgetField,
                            jEntityTarget, currField, null, varName, fieldsToMap));

                    if (g == null) {
                        throw new RuntimeException("Cannot generateGetField mapper for widget: " + jEntityTarget.getName());
                    }

                    for (JField fld : fieldsToMap) {
                        String fieldName = fld.getName();

                        JField targetField = jEntityTarget.getField(fieldName);
                        if (targetField == null) {
                            throw new RuntimeException("The field '" + fieldName + "' does not correspond with a field in the class: "
                                    + jEntityTarget.getQualifiedSourceName());
                        }

                        generatedBindings.add(
                                g.generateFieldMapperGenerator(typeOracle, widgetField, jEntityTarget, null, targetField)
                        );
                    }


                    Map<String, Object> vars = new HashMap<String, Object>();
                    vars.put("typeOracle", typeOracle);
                    vars.put("variableName", varName);
                    vars.put("strTypeParms", strTypeParms);
                    vars.put("targetWidget", widgetField.getType().getQualifiedSourceName());
                    vars.put("targetType", jEntityTarget.getQualifiedSourceName());
                    vars.put("initializers", generatedInitializations);
                    vars.put("bindings", generatedBindings);
                    vars.put("targetFieldName", widgetField.getName());

                    String s = (String) TemplateRuntime.execute(mappingsGen, vars);
                    sourceWriter.print(s);

                    s = "widget." + currField.getName() + " = " + varName + ";";

                    sourceWriter.println(s);

                } else if (currField.isAnnotationPresent(EntityMapped.class)) {
                    EntityMapped entityMappedA = currField.getAnnotation(EntityMapped.class);

                    JClassType entityType = currField.getType().isClassOrInterface();
                    String varName = currField.getName() + "Mapper";

                    String entityFieldName = currField.getName();
                    String toEntityField;

                    Map<String, List<JField>> toBeMapped = new HashMap<String, List<JField>>();

                    for (JField fld : targetClass.getFields()) {
                        if (fld.isAnnotationPresent(MapField.class)) {
                            MapField mapFieldA = fld.getAnnotation(MapField.class);
                            toEntityField = "".equals(mapFieldA.value()) ? entityFieldName : mapFieldA.value();

                            if (!toBeMapped.containsKey(toEntityField)) {
                                toBeMapped.put(toEntityField, new LinkedList<JField>());
                            }

                            toBeMapped.get(toEntityField).add(fld);
                        }
                    }

                    /**
                     * Generate the field mappings.
                     */
                    for (Map.Entry<String, List<JField>> entry : toBeMapped.entrySet()) {
                        List<String> generatedInitializations = new LinkedList<String>();
                        List<String> generatedBindings = new LinkedList<String>();
                        ArrayList<String[]> fieldIndexPositions = new ArrayList<String[]>();

                        for (JField fld : entry.getValue()) {
                            JClassType classType = fld.getType().isClassOrInterface();

                            String fieldName = fld.getAnnotation(MapField.class).value();
                            if ("".equals(fieldName)) {
                                fieldName = fld.getName();
                            }

                            JField targetField = entityType.getField(fieldName);
                            if (targetField == null) {
                                throw new RuntimeException("The field '" + fieldName + "' does not correspond with a field in the class: "
                                        + entityType.getQualifiedSourceName());
                            }

                            JClassType targetFieldType = targetField.getType().isClassOrInterface();

                            FieldMapperGenerator g =
                                    getFieldMapper(classType.getQualifiedSourceName());

                            if (g == null) {
                                throw new RuntimeException("Cannot generateGetField mapper for widget: " + classType.getName());
                            }

                            generatedInitializations.add(g.init(typeOracle, fld,
                                    entityType, targetField, currField, null, null));

                            generatedBindings.add(
                                    g.generateFieldMapperGenerator(typeOracle,
                                            fld,
                                            entityType,
                                            targetField,
                                            currField));


                            if (getType(typeOracle, Widget.class).isAssignableFrom(classType)) {
                                fieldIndexPositions.add(new String[]{fieldName,
                                        g.generateValueExtractorStatement(typeOracle,
                                                fld,
                                                entityType,
                                                targetField,
                                                currField)});
                            }
                        }

                        Map<String, Object> vars = new HashMap<String, Object>();
                        vars.put("typeOracle", typeOracle);
                        vars.put("variableName", varName);
                        vars.put("initializers", generatedInitializations);
                        vars.put("targetFieldName", entityFieldName);
                        vars.put("bindings", generatedBindings);
                        vars.put("fieldIndexPositions", fieldIndexPositions);
                        vars.put("entityFieldName", entityFieldName);

                        String s = (String) TemplateRuntime.execute(entityMappingGen, vars);

                        sourceWriter.print(s);
                    }
                } else if (currField.isAnnotationPresent(AddAllTo.class)) {
                    String copyToField = currField.getAnnotation(AddAllTo.class).value();
                    String copyFromField = currField.getName();

                    Map<String, Object> vars = new HashMap<String, Object>();
                    vars.put("copyToField", copyToField);
                    vars.put("copyFromField", copyFromField);

                    String s = (String) TemplateRuntime.execute(addAllToGen, vars);

                    sourceWriter.print(s);
                }
            }
        }
        catch (Exception e) {
            logger.log(TreeLogger.Type.ERROR, "failed to map field (does not exist)", e);
            e.printStackTrace();
        }

        // end constructor source generation
        sourceWriter.outdent();
        sourceWriter.println("}");
    }

    private String generateTypeParmString(JParameterizedType t) {
        StringBuilder builder = new StringBuilder("<");
        JClassType[] typeArgs = t.getTypeArgs();
        for (int i = 0, typeArgsLength = typeArgs.length; i < typeArgsLength; i++) {
            JClassType c = typeArgs[i];
            builder.append(c.getQualifiedSourceName());

            JParameterizedType pt = c.isParameterized();
            if (pt != null) builder.append(generateTypeParmString(pt));

            if ((i + 1) < typeArgsLength) builder.append(",");
        }
        return builder.append(">").toString();
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
