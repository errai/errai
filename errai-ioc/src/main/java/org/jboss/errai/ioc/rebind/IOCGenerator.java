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
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.inject.Inject;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.rebind.ProcessingContext;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.util.RebindVisitor;
import org.jboss.errai.ioc.client.InterfaceInjectionContext;
import org.jboss.errai.ioc.client.api.*;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;
import org.mvel2.util.Make;
import org.mvel2.util.ReflectionUtil;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.*;

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

    private IOCFactory iocFactory;
    private ProcessorFactory procFactory = new ProcessorFactory();

    private Map<String, List<Expression>> deferredExpressions = new HashMap<String, List<Expression>>();
    private Map<JClassType, List<Runnable>> deferredTasks = new HashMap<JClassType, List<Runnable>>();

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

        iocFactory = new IOCFactory(typeOracle);
        final JClassType widgetType = getJClassType(Widget.class);

        procFactory.registerHandler(ToRootPanel.class, new AnnotationHandler<ToRootPanel>() {
            public void handle(JClassType type, ToRootPanel annotation, ProcessingContext context) {
                if (widgetType.isAssignableFrom(type)) {
                    context.getWriter().println("ctx.addToRootPanel(" + generateInjectors(annotation, context, iocFactory, type) + ");");
                } else {
                    throw new RuntimeException("Type declares @" + annotation.getClass().getSimpleName()
                            + "  but does not extend type Widget: " + type.getQualifiedSourceName());
                }
            }
        });

        procFactory.registerHandler(CreatePanel.class, new AnnotationHandler<CreatePanel>() {
            public void handle(JClassType type, CreatePanel annotation, ProcessingContext context) {
                if (widgetType.isAssignableFrom(type)) {
                    SourceWriter writer = context.getWriter();
                    writer.println("ctx.registerPanel(\"" + (annotation.value().equals("") ? type.getName() : annotation.value()) + "\", " + generateInjectors(annotation, context, iocFactory, type) + ");");
                } else {
                    throw new RuntimeException("Type declares @" + annotation.getClass().getSimpleName()
                            + "  but does not extend type Widget: " + type.getQualifiedSourceName());
                }
            }
        });

        procFactory.registerHandler(ToPanel.class, new AnnotationHandler<ToPanel>() {
            public void handle(JClassType type, ToPanel annotation, ProcessingContext context) {
                if (widgetType.isAssignableFrom(type)) {
                    SourceWriter writer = context.getWriter();
                    writer.println("ctx.widgetToPanel(" + generateInjectors(annotation, context, iocFactory, type) + ", \"" + annotation.value() + "\");");
                } else {
                    throw new RuntimeException("Type declares @" + annotation.getClass().getSimpleName()
                            + "  but does not extend type Widget: " + type.getQualifiedSourceName());
                }
            }
        });


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
        composer.addImport(InterfaceInjectionContext.class.getName());
        composer.addImport(Widget.class.getName());
        composer.addImport(List.class.getName());
        composer.addImport(ArrayList.class.getName());
        composer.addImport(Map.class.getName());
        composer.addImport(HashMap.class.getName());
        composer.addImport(com.google.gwt.user.client.ui.Panel.class.getName());
        composer.addImport(ErraiBus.class.getName());

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

        sourceWriter.println("public InterfaceInjectionContext bootstrapContainer() { ");
        sourceWriter.outdent();
        sourceWriter.println("InterfaceInjectionContext ctx = new InterfaceInjectionContext();");

        final List<File> targets = findAllConfigTargets();

        final ProcessingContext procContext = new ProcessingContext(logger, context, sourceWriter, typeOracle);

        final JClassType typeProviderCls;

        try {
            typeProviderCls = typeOracle.getType(TypeProvider.class.getName());
        }
        catch (NotFoundException e) {
            throw new RuntimeException(e);
        }

        visitAllTargets(targets, context, logger, sourceWriter, typeOracle, new RebindVisitor() {
            public void visit(JClassType visit, GeneratorContext context, TreeLogger logger, SourceWriter writer) {
                if (visit.isAnnotationPresent(Provider.class)) {
                    JClassType bindType = null;

                    for (JClassType iface : visit.getImplementedInterfaces()) {
                        if (!typeProviderCls.isAssignableFrom(iface)) {
                            continue;
                        }

                        JParameterizedType pType = iface.isParameterized();

                        if (pType == null) {
                            throw new RuntimeException("could not determine the bind type for the Provider class: " + visit.getQualifiedSourceName());
                        }

                        bindType = pType.getTypeArgs()[0];
                    }

                    if (bindType == null) {
                        throw new RuntimeException("the annotated provider class does not appear to implement " + TypeProvider.class.getName() + ": " + visit.getQualifiedSourceName());
                    }

                    iocFactory.registerTypeProvider(bindType, visit);
                }
            }

            public void visitError(String className, Throwable t) {
            }
        });

        visitAllTargets(targets, context, logger, sourceWriter, typeOracle,
                new RebindVisitor() {
                    public void visit(JClassType visitC, GeneratorContext context, TreeLogger logger, SourceWriter writer) {
                        procFactory.process(visitC, procContext);
                    }

                    public void visitError(String className, Throwable t) {
                    }
                }

        );

        for (Map.Entry<String, List<Expression>> entry : deferredExpressions.entrySet()) {
            for (Expression pair : entry.getValue()) {
                sourceWriter.println(entry.getKey() + "." + pair.toString() + ";");
            }
        }

        sourceWriter.println(" return ctx;");
        sourceWriter.outdent();
        sourceWriter.println("}");
    }


    public String generateInjectors(final Annotation annotation,
                                    final ProcessingContext context,
                                    final IOCFactory iocFactory,
                                    final JClassType visit) {


        if (context.hasProcessed(visit)) {
            return context.getProcessed(visit).getVarName();
        }

        final String varName = "widget" + ++varCount;


        try {
            List<Expression> setterPairs = new LinkedList<Expression>();

            Map<String, String> fieldToServices = new HashMap<String, String>();
            String postConstruct = null;
            javax.annotation.PostConstruct postConstructAnnotation = null;
            for (JMethod m : visit.getMethods()) {
                if (m.isAnnotationPresent(javax.annotation.PostConstruct.class)) {
                    postConstruct = m.getName();
                    postConstructAnnotation = m.getAnnotation(javax.annotation.PostConstruct.class);
                }
            }

            if (postConstruct != null && context.hasProcessed(visit)
                    && context.getProcessed(visit).hasAnnotation(postConstructAnnotation)) {
                postConstruct = null;
            }

            if (postConstruct != null) {
                addDeferredExpression(varName, new Expression(false, postConstruct, ""));
                context.addProcessed(varName, visit, postConstructAnnotation);
            }

            for (JConstructor c : visit.getConstructors()) {
                if (c.isAnnotationPresent(Inject.class) || c.isAnnotationPresent(javax.inject.Inject.class)) {
                    JParameter[] parameterTypes = c.getParameters();
                    List<String> constructorExpr = new ArrayList<String>(parameterTypes.length);

                    for (JParameter pType : parameterTypes) {
                        constructorExpr.add(iocFactory.getInjectorExpression(pType.getType().isClassOrInterface()));
                    }


                    String s = (String) TemplateRuntime.execute(widgetBuild, Make.Map.<String, Object>$()
                            ._("widgetClassName", visit.getQualifiedSourceName())
                            ._("varName", varName)
                            ._("postConstruct", null)
                            ._("constructorInjection", true)
                            ._("fieldToServices", fieldToServices)
                            ._("setterPairs", setterPairs)
                            ._("constructorExpressions", constructorExpr)._());

                    s = s.replaceAll("\n", "").replaceAll(";", ";\n");

                    context.getWriter().println(s);

                    context.addProcessed(varName, visit, annotation);

                    fireLoaded(visit);

                    return varName;
                }
            }


            for (final JField f : visit.getFields()) {
                if (f.isAnnotationPresent(Inject.class) || f.isAnnotationPresent(javax.inject.Inject.class)) {
                    JClassType fieldType = f.getType().isClassOrInterface();

                    JClassType providerType = iocFactory.getTypeProvider(fieldType);

                    if (providerType == null) {
                        throw new RuntimeException("no available provider for type: " + fieldType.getQualifiedSourceName());
                    }

                    final String expr = "new " + providerType.getQualifiedSourceName() + "().provide()";

                    try {
                        visit.getMethod(ReflectionUtil.getSetter(f.getName()), new JType[0]);

                        setterPairs.add(new Expression(false, ReflectionUtil.getSetter(f.getName()), expr));
                    }
                    catch (NotFoundException e) {
                        setterPairs.add(new Expression(true, f.getName(), expr));
                    }
                } else if (f.isAnnotationPresent(Service.class)) {
                    Service svc = f.getAnnotation(Service.class);
                    String name;
                    if (svc.value().equals("")) {
                        name = f.getName();
                    } else {
                        name = svc.value();
                    }

                    fieldToServices.put(f.getName(), name);
                } else if (f.isAnnotationPresent(InjectPanel.class)) {
                    final JClassType t = f.getType().isClassOrInterface();

                    if (!context.hasProcessed(t)) {
                        addDeferred(t, new Runnable() {
                            public void run() {
                                try {
                                    visit.getMethod(ReflectionUtil.getSetter(f.getName()), new JType[0]);
                                    addFirstOrderDeferredExpression(varName, new Expression(false, ReflectionUtil.getSetter(f.getName()), context.getProcessed(t).getVarName()));
                                }
                                catch (NotFoundException e) {
                                    addFirstOrderDeferredExpression(varName, new Expression(true, f.getName(), context.getProcessed(t).getVarName()));
                                }
                            }
                        });

                    } else {
                        try {
                            visit.getMethod(ReflectionUtil.getSetter(f.getName()), new JType[0]);
                            setterPairs.add(new Expression(false, ReflectionUtil.getSetter(f.getName()), context.getProcessed(t).getVarName()));
                        }
                        catch (NotFoundException e) {
                            setterPairs.add(new Expression(true, f.getName(), context.getProcessed(t).getVarName()));
                        }
                    }
                }

            }

            String s = (String) TemplateRuntime.execute(widgetBuild, Make.Map.<String, Object>$()
                    ._("widgetClassName", visit.getQualifiedSourceName())
                    ._("varName", varName)
                    ._("constructorInjection", false)
                    ._("setterPairs", setterPairs)
                    ._("postConstruct", null)
                    ._("fieldToServices", fieldToServices)
                    ._());

            s = s.replaceAll("\n", "").replaceAll(";", ";\n");

            context.getWriter().println(s);

            context.addProcessed(varName, visit, annotation);

            fireLoaded(visit);

            return varName;
        }
        catch (Exception e) {
            throw new RuntimeException("Could not create type: " + visit.getName(), e);
        }
    }

    public void addDeferred(JClassType type, Runnable task) {
        if (!deferredTasks.containsKey(type))
            deferredTasks.put(type, new LinkedList<Runnable>());

        deferredTasks.get(type).add(task);
    }

    public void addFirstOrderDeferredExpression(String name, Expression pair) {
        if (!deferredExpressions.containsKey(name))
            deferredExpressions.put(name, new LinkedList<Expression>());

        deferredExpressions.get(name).add(0, pair);

    }

    public void addDeferredExpression(String name, Expression pair) {
        if (!deferredExpressions.containsKey(name))
            deferredExpressions.put(name, new LinkedList<Expression>());

        deferredExpressions.get(name).add(pair);
    }

    public void fireLoaded(JClassType type) {
        if (deferredTasks.containsKey(type)) {
            for (Runnable run : deferredTasks.get(type)) {
                run.run();
            }
        }
    }

    public JClassType getJClassType(Class cls) {
        try {
            return typeOracle.getType(cls.getName());
        }
        catch (NotFoundException e) {
            return null;
        }
    }
}
