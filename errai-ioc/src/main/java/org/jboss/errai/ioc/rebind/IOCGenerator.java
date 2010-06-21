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

package org.jboss.errai.ioc.rebind;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.*;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.inject.Inject;
import org.jboss.errai.bus.server.util.RebindVisitor;
import org.jboss.errai.ioc.client.api.Bootstrapper;
import org.jboss.errai.ioc.client.api.ToRootPanel;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;
import org.mvel2.util.Make;
import org.mvel2.util.PropertyTools;
import org.mvel2.util.ReflectionUtil;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.jboss.errai.bus.server.util.ConfigUtil.*;

public class IOCGenerator extends Generator {
    /**
     * Simple name of class to be generated
     */
    private String className = null;

    /**
     * Package name of class to be generated
     */
    private String packageName = null;

    private TypeOracle typeOracle;

    private final CompiledTemplate widgetBuild;

    private int varCount = 0;

    public IOCGenerator() {
        InputStream istream = this.getClass().getResourceAsStream("WidgetBuild.mv");
        widgetBuild = TemplateCompiler.compileTemplate(istream);
    }

    public IOCGenerator(TypeOracle typeOracle) {
        this();
        this.typeOracle = typeOracle;
    }

    @Override
    public String generate(TreeLogger logger, GeneratorContext context, String typeName)
            throws UnableToCompleteException {
        typeOracle = context.getTypeOracle();

        try {
            // get classType and save instance variables

            JClassType classType = typeOracle.getType(typeName);
            packageName = classType.getPackage().getName();
            className = classType.getSimpleSourceName() + "Impl";

            logger.log(TreeLogger.INFO, "Generating Extensions Bootstrapper...");

            // Generate class source code
            generateClass(logger, context);

        }
        catch (Throwable e) {

            // record sendNowWith logger that Map generation threw an exception
            e.printStackTrace();
            logger.log(TreeLogger.ERROR, "Error generating extensions", e);

        }

        // return the fully qualified name of the class generated
        return packageName + "." + className;
    }

    /**
     * Generate source code for new class. Class extends
     * <code>HashMap</code>.
     *
     * @param logger  Logger object
     * @param context Generator context
     */
    private void generateClass(TreeLogger logger, GeneratorContext context) {

        // get print writer that receives the source code
        PrintWriter printWriter = context.tryCreate(logger, packageName, className);
        // print writer if null, source code has ALREADY been generated,

        if (printWriter == null) return;

        // init composer, set class properties, create source writer
        ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(packageName,
                className);

        composer.addImplementedInterface(Bootstrapper.class.getName());
        composer.addImport(Widget.class.getName());
        composer.addImport(List.class.getName());
        composer.addImport(ArrayList.class.getName());

        SourceWriter sourceWriter = composer.createSourceWriter(context, printWriter);

        // generator constructor source code
        generateExtensions(context, logger, sourceWriter);
        // close generated class
        sourceWriter.outdent();
        sourceWriter.println("}");


        // commit generated class
        context.commit(logger, printWriter);
    }

    private void generateExtensions(final GeneratorContext context, final TreeLogger logger, final SourceWriter sourceWriter) {
        // start constructor source generation
        sourceWriter.println("public " + className + "() { ");
        sourceWriter.indent();
        sourceWriter.println("super();");
        sourceWriter.outdent();
        sourceWriter.println("}");

        sourceWriter.println("public List<Widget> bootstrapContainer() { ");
        sourceWriter.outdent();
        sourceWriter.println("List<Widget> widgets = new ArrayList<Widget>();");

        final List<File> targets = findAllConfigTargets();

        final IOCFactory iocFactory = new IOCFactory(typeOracle);

        visitAllTargets(targets, context, logger, sourceWriter,
                new RebindVisitor() {
                    public void visit(Class<?> visit, GeneratorContext context, TreeLogger logger, SourceWriter writer) {
                        try {
                            JClassType visitC = typeOracle.getType(visit.getName());
                            if (visitC.isAssignableTo(typeOracle.getType(Widget.class.getName())) && visit.isAnnotationPresent(ToRootPanel.class)) {
                                String widgetName = generateInjectors(context, logger, sourceWriter, iocFactory, className, visitC);
                                sourceWriter.println("widgets.add(" + widgetName + ");");
                            }
                        }
                        catch (NotFoundException e) {
                        }
                    }

                    public void visitError(String className, Throwable t) {
                        try {
                            JClassType visit = typeOracle.getType(className);
                            JClassType widgetType = typeOracle.getType(Widget.class.getName());
                            if (visit.isAssignableTo(widgetType) && visit.isAnnotationPresent(ToRootPanel.class)) {
                                String widgetName =  generateInjectors(context, logger, sourceWriter, iocFactory, className, visit);
                                sourceWriter.println("widgets.add(" + widgetName + ");");
                            }
                        }
                        catch (NotFoundException e) {
                        }
                    }
                }

        );

        sourceWriter.println(" return widgets;");
        sourceWriter.outdent();
        sourceWriter.println("}");
    }

    public String generateInjectors(final GeneratorContext context, final TreeLogger logger,
                                   final SourceWriter sourceWriter,
                                   final IOCFactory iocFactory,
                                   final String className,
                                   final JClassType visit) {

        try {
            for (JConstructor c : visit.getConstructors()) {
                if (c.isAnnotationPresent(Inject.class)) {
                    JParameter[] parameterTypes = c.getParameters();
                    List<String> constructorExpr = new ArrayList<String>(parameterTypes.length);

                    for (JParameter pType : parameterTypes) {
                        constructorExpr.add(iocFactory.getInjectorExpression(pType.getType().isClassOrInterface()));
                    }

                    String s = (String) TemplateRuntime.execute(widgetBuild, Make.Map.<String, Object>$()
                            ._("widgetClassName", className)
                            ._("varName", "widget" + (++varCount))
                            ._("constructorInjection", true)
                            ._("constructorExpressions", constructorExpr)._());

                    sourceWriter.println(s);

                    return "widget" + varCount;
                }
            }

            List<SetterPair> setterPairs = new LinkedList<SetterPair>();

            for (JField f : visit.getFields()) {
                if (f.isAnnotationPresent(Inject.class)) {
                    try {
                        visit.getMethod(ReflectionUtil.getSetter(f.getName()), new JType[0]);
                        setterPairs.add(new SetterPair(true, f.getName(), iocFactory.getInjectorExpression(f.getType().isClassOrInterface())));
                    }
                    catch (NotFoundException e) {
                        setterPairs.add(new SetterPair(false, ReflectionUtil.getSetter(f.getName()), iocFactory.getInjectorExpression(f.getType().isClassOrInterface())));
                    }
                }
            }


            String s = (String) TemplateRuntime.execute(widgetBuild, Make.Map.<String, Object>$()
                    ._("widgetClassName", className)
                    ._("varName", "widget" + (++varCount))
                    ._("constructorInjection", false)
                    ._("setterPairs", setterPairs)._());

            sourceWriter.println(s);

            return "widget" + varCount;
        }
        catch (Exception e) {
            throw new RuntimeException("Could  ot create type: " + visit.getName(), e);
        }
    }


}
