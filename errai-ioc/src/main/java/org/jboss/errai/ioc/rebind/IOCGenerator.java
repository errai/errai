/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
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

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.rebind.ScannerSingleton;
import org.jboss.errai.bus.server.ErraiBootstrapFailure;
import org.jboss.errai.bus.server.service.metadata.MetaDataScanner;
import org.jboss.errai.ioc.client.InterfaceInjectionContext;
import org.jboss.errai.ioc.client.api.Bootstrapper;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.CreatePanel;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.api.GeneratedBy;
import org.jboss.errai.ioc.client.api.IOCProvider;
import org.jboss.errai.ioc.client.api.ToPanel;
import org.jboss.errai.ioc.client.api.ToRootPanel;
import org.jboss.errai.ioc.client.api.TypeProvider;
import org.jboss.errai.ioc.rebind.ioc.ContextualProviderInjector;
import org.jboss.errai.ioc.rebind.ioc.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.IOCExtensionConfigurator;
import org.jboss.errai.ioc.rebind.ioc.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.InjectionFailure;
import org.jboss.errai.ioc.rebind.ioc.Injector;
import org.jboss.errai.ioc.rebind.ioc.InjectorFactory;
import org.jboss.errai.ioc.rebind.ioc.ProviderInjector;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.DefParameters;
import org.jboss.errai.ioc.rebind.ioc.codegen.Modifier;
import org.jboss.errai.ioc.rebind.ioc.codegen.Parameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.StringStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.BlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameterizedType;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.build.BuildMetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.JSNIUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.Stmt;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.user.rebind.StringSourceWriter;

/**
 * The main generator class for the errai-ioc framework.
 */
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

  private IOCProcessingContext procContext;
  private InjectorFactory injectFactory;
  private ProcessorFactory procFactory;

  public static final boolean isDebugCompile = Boolean.getBoolean("errai.ioc.debug");

  private List<Runnable> deferredTasks = new LinkedList<Runnable>();

  public IOCGenerator() {
  }

  public IOCGenerator(IOCProcessingContext processingContext) {
    this();
    this.procContext = processingContext;
    this.typeOracle = processingContext.getOracle();
    this.injectFactory = new InjectorFactory(processingContext);
    this.procFactory = new ProcessorFactory(injectFactory);
    defaultConfigureProcessor();
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
      generateIOCBootstrapClass(logger, context);
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
  private void generateIOCBootstrapClass(TreeLogger logger, GeneratorContext context) {
    // get print writer that receives the source code
    PrintWriter printWriter = context.tryCreate(logger, packageName, className);
    // print writer if null, source code has ALREADY been generated,

    if (printWriter == null) return;

    ClassStructureBuilder<?> classStructureBuilder = ClassBuilder.define(packageName + "." + className)
            .publicScope()
            .implementsInterface(Bootstrapper.class)
            .body();

    BuildMetaClass bootStrapClass = classStructureBuilder.getClassDefinition();
    Context buildContext = bootStrapClass.getContext();

    BlockBuilder<?> blockBuilder =
            classStructureBuilder.publicMethod(InterfaceInjectionContext.class, "bootstrapContainer");

    SourceWriter sourceWriter = new StringSourceWriter();

    procContext = new IOCProcessingContext(logger, context, sourceWriter,
            typeOracle, buildContext, bootStrapClass, blockBuilder);

    injectFactory = new InjectorFactory(procContext);
    procFactory = new ProcessorFactory(injectFactory);
    defaultConfigureProcessor();

    // generator constructor source code
    initializeProviders();
    generateExtensions(sourceWriter, classStructureBuilder, blockBuilder);
    // close generated class


    printWriter.append(sourceWriter.toString());
    // commit generated class
    context.commit(logger, printWriter);

  }

  public void initializeProviders() {

    final MetaClass typeProviderCls = MetaClassFactory.get(typeOracle, TypeProvider.class);
    MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();
    /*
    * IOCDecoratorExtension.class
    */
    Set<Class<?>> iocExtensions = scanner.getTypesAnnotatedWith(org.jboss.errai.ioc.client.api.IOCExtension.class);
    List<IOCExtensionConfigurator> extensionConfigurators = new ArrayList<IOCExtensionConfigurator>();
    for (Class<?> clazz : iocExtensions) {
      try {
        Class<? extends IOCExtensionConfigurator> configuratorClass = clazz.asSubclass(IOCExtensionConfigurator.class);

        IOCExtensionConfigurator configurator = configuratorClass.newInstance();

        configurator.configure(procContext, injectFactory, procFactory);

        extensionConfigurators.add(configurator);
      }
      catch (Exception e) {
        throw new ErraiBootstrapFailure("unable to load IOC Extension Configurator: " + e.getMessage(), e);
      }
    }

    /**
     * CodeDecorator.class
     */
    Set<Class<?>> decorators = scanner.getTypesAnnotatedWith(CodeDecorator.class);
    for (Class<?> clazz : decorators) {
      try {
        Class<? extends IOCDecoratorExtension> decoratorClass = clazz.asSubclass(IOCDecoratorExtension.class);

        Class<? extends Annotation> annoType = null;
        Type t = decoratorClass.getGenericSuperclass();
        if (!(t instanceof ParameterizedType)) {
          throw new ErraiBootstrapFailure("code decorator must extend IOCDecoratorExtension<@AnnotationType>");
        }

        ParameterizedType pType = (ParameterizedType) t;
        if (IOCDecoratorExtension.class.equals(pType.getRawType())) {
          if (pType.getActualTypeArguments().length == 0
                  || !Annotation.class.isAssignableFrom((Class) pType.getActualTypeArguments()[0])) {
            throw new ErraiBootstrapFailure("code decorator must extend IOCDecoratorExtension<@AnnotationType>");
          }

          annoType = ((Class) pType.getActualTypeArguments()[0]).asSubclass(Annotation.class);
        }

        injectFactory.getInjectionContext().registerDecorator(
                decoratorClass.getConstructor(new Class[]{Class.class}).newInstance(annoType));
      }
      catch (Exception e) {
        throw new ErraiBootstrapFailure("unable to load code decorator: " + e.getMessage(), e);
      }
    }

    /**
     * IOCProvider.class
     */
    Set<Class<?>> providers = scanner.getTypesAnnotatedWith(IOCProvider.class);
    for (Class<?> clazz : providers) {
      MetaClass bindType = null;
      MetaClass type = MetaClassFactory.get(typeOracle, clazz);

      boolean contextual = false;
      for (MetaClass iface : type.getInterfaces()) {
        if (iface.getFullyQualifiedName().equals(ContextualTypeProvider.class.getName())) {
          contextual = true;

          MetaParameterizedType pType = iface.getParameterizedType();

          if (pType == null) {
            throw new InjectionFailure("could not determine the bind type for the IOCProvider class: "
                    + type.getFullyQualifiedName());
          }

          //todo: check for nested type parameters
          bindType = (MetaClass) pType.getTypeParameters()[0];
          break;
        }
      }

      if (bindType == null) {
        for (MetaClass iface : type.getInterfaces()) {
          if (!typeProviderCls.isAssignableFrom(iface)) {
            continue;
          }

          MetaParameterizedType pType = iface.getParameterizedType();

          if (pType == null) {
            throw new InjectionFailure("could not determine the bind type for the IOCProvider class: "
                    + type.getFullyQualifiedName());
          }

          //todo: check for nested type parameters
          bindType = (MetaClass) pType.getTypeParameters()[0];
        }
      }

      if (bindType == null) {
        throw new InjectionFailure("the annotated provider class does not appear to implement " +
                TypeProvider.class.getName() + ": " + type.getFullyQualifiedName());
      }

      final MetaClass finalBindType = bindType;

      Injector injector;
      if (contextual) {
        injector = new ContextualProviderInjector(finalBindType, type);
      }
      else {
        injector = new ProviderInjector(finalBindType, type);
      }

      injectFactory.addInjector(injector);
    }

    /**
     * GeneratedBy.class
     */
    Set<Class<?>> generatedBys = scanner.getTypesAnnotatedWith(GeneratedBy.class);
    for (Class<?> clazz : generatedBys) {
      MetaClass type = MetaClassFactory.get(typeOracle, clazz);
      GeneratedBy anno = type.getAnnotation(GeneratedBy.class);
      Class<? extends ContextualTypeProvider> injectorClass = anno.value();

      try {
        injectFactory.addInjector(new ContextualProviderInjector(type, getJClassType(injectorClass)));
      }
      catch (Exception e) {
        throw new ErraiBootstrapFailure("could not load injector: " + e.getMessage(), e);
      }
    }

    for (IOCExtensionConfigurator extensionConfigurator : extensionConfigurators) {
      extensionConfigurator.afterInitialization(procContext, injectFactory, procFactory);
    }
  }

  private void generateExtensions(SourceWriter sourceWriter, ClassStructureBuilder<?> classBuilder,
                                  BlockBuilder<?> blockBuilder) {
    blockBuilder.append(Stmt.declareVariable("ctx", InterfaceInjectionContext.class,
            Stmt.newObject(InterfaceInjectionContext.class)));

    MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();
    procFactory.process(scanner, procContext);
    procFactory.processAll();

    runAllDeferred();

    blockBuilder.append(Stmt.loadVariable("ctx").returnValue());

    Collection<MetaField> privateFields = injectFactory.getInjectionContext().getPrivateFieldsToExpose();
    for (MetaField f : privateFields) {
      addJSNIStubs(classBuilder, f, f.getType());
    }

    blockBuilder.finish();

    String generated = classBuilder.toJavaString();

    if (Boolean.getBoolean("errai.ioc.generator.print_out_result")) {
      System.out.println("----Emitting Class--->\n\n");
      System.out.println(generated);
      System.out.println("<---Emitting Class----");
    }

    sourceWriter.print(generated);
  }

  private static void addJSNIStubs(ClassStructureBuilder<?> classBuilder, MetaField f, MetaClass type) {
    classBuilder.privateMethod(void.class, InjectUtil.getPrivateFieldInjectorName(f))
            .parameters(DefParameters.fromParameters(Parameter.of(f.getDeclaringClass(), "instance"),
                    Parameter.of(type, "value")))
            .modifiers(Modifier.Static, Modifier.JSNI)
            .body()
            .append(new StringStatement(JSNIUtil.fieldAccess(f) + " = value"))
            .finish();

    classBuilder.privateMethod(type, InjectUtil.getPrivateFieldInjectorName(f))
            .parameters(DefParameters.fromParameters(Parameter.of(f.getDeclaringClass(), "instance")))
            .modifiers(Modifier.Static, Modifier.JSNI)
            .body()
            .append(new StringStatement("return " + JSNIUtil.fieldAccess(f)))
            .finish();
  }

  public void addType(final MetaClass type) {
    injectFactory.addType(type);
  }

  public Statement generateWithSingletonSemantics(final MetaClass visit) {
    return injectFactory.generateSingleton(visit);
  }

  public Statement generateInjectors(final MetaClass visit) {
    return injectFactory.generate(visit);
  }

  public String generateAllProviders() {
    return injectFactory.generateAllProviders();
  }

  public void addDeferred(Runnable task) {
    deferredTasks.add(task);
  }

  private void runAllDeferred() {
    injectFactory.getInjectionContext().runAllDeferred();

    for (Runnable r : deferredTasks)
      r.run();
  }

  public MetaClass getJClassType(Class cls) {
    try {
      return MetaClassFactory.get(typeOracle.getType(cls.getName()));
    }
    catch (NotFoundException e) {
      return null;
    }
  }

  private void defaultConfigureProcessor() {
    final MetaClass widgetType = getJClassType(Widget.class);
    final MetaClass messageCallbackType = getJClassType(MessageCallback.class);
    final MetaClass messageBusType = getJClassType(MessageBus.class);

    procFactory.registerHandler(EntryPoint.class, new AnnotationHandler<EntryPoint>() {
      @Override
      public boolean handle(final InjectableInstance type, EntryPoint annotation, IOCProcessingContext context) {
        generateWithSingletonSemantics(type.getType());
        return true;
      }
    });

    procFactory.registerHandler(ToRootPanel.class, new AnnotationHandler<ToRootPanel>() {
      @Override
      public boolean handle(final InjectableInstance type, final ToRootPanel annotation,
                            final IOCProcessingContext context) {
        if (widgetType.isAssignableFrom(type.getType())) {

          addDeferred(new Runnable() {
            @Override
            public void run() {
              context.getWriter().println("ctx.addToRootPanel(" + generateWithSingletonSemantics(type.getType()) + ");");
            }
          });
        }
        else {
          throw new InjectionFailure("type declares @" + annotation.getClass().getSimpleName()
                  + "  but does not extend type Widget: " + type.getType().getFullyQualifiedName());
        }

        return true;
      }
    });

    procFactory.registerHandler(CreatePanel.class, new AnnotationHandler<CreatePanel>() {
      @Override
      public boolean handle(final InjectableInstance type, final CreatePanel annotation,
                            final IOCProcessingContext context) {
        if (widgetType.isAssignableFrom(type.getType())) {

          addDeferred(new Runnable() {
            @Override
            public void run() {
              context.getWriter().println("ctx.registerPanel(\"" + (annotation.value().equals("")
                      ? type.getType().getName() : annotation.value()) + "\", " + generateInjectors(type.getType()) + ");");
            }
          });
        }
        else {
          throw new InjectionFailure("type declares @" + annotation.getClass().getSimpleName()
                  + "  but does not extend type Widget: " + type.getType().getFullyQualifiedName());
        }
        return true;
      }
    });

    procFactory.registerHandler(ToPanel.class, new AnnotationHandler<ToPanel>() {
      @Override
      public boolean handle(final InjectableInstance type, final ToPanel annotation,
                            final IOCProcessingContext context) {
        if (widgetType.isAssignableFrom(type.getType())) {

          addDeferred(new Runnable() {
            @Override
            public void run() {
              context.getWriter()
                      .println("ctx.widgetToPanel(" + generateWithSingletonSemantics(type.getType())
                              + ", \"" + annotation.value() + "\");");
            }
          });
        }
        else {
          throw new InjectionFailure("type declares @" + annotation.getClass().getSimpleName()
                  + "  but does not extend type Widget: " + type.getType().getFullyQualifiedName());
        }
        return true;
      }
    });
  }
}
