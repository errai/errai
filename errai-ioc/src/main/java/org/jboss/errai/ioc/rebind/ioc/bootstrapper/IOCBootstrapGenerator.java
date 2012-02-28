/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.ioc.rebind.ioc.bootstrapper;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.user.rebind.StringSourceWriter;
import com.google.inject.servlet.RequestScoped;
import org.jboss.errai.bus.server.ErraiBootstrapFailure;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.builder.BlockBuilder;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.meta.MetaParameterizedType;
import org.jboss.errai.codegen.framework.meta.MetaType;
import org.jboss.errai.codegen.framework.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.framework.meta.impl.gwt.GWTClass;
import org.jboss.errai.codegen.framework.util.GenUtil;
import org.jboss.errai.codegen.framework.util.Implementations;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.ioc.client.ContextualProviderContext;
import org.jboss.errai.ioc.client.InterfaceInjectionContext;
import org.jboss.errai.ioc.client.api.Bootstrapper;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.CreatePanel;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.api.GeneratedBy;
import org.jboss.errai.ioc.client.api.IOCBootstrapTask;
import org.jboss.errai.ioc.client.api.IOCProvider;
import org.jboss.errai.ioc.client.api.TaskOrder;
import org.jboss.errai.ioc.client.api.ToPanel;
import org.jboss.errai.ioc.client.api.ToRootPanel;
import org.jboss.errai.ioc.client.api.TypeProvider;
import org.jboss.errai.ioc.rebind.AnnotationHandler;
import org.jboss.errai.ioc.rebind.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.IOCProcessorFactory;
import org.jboss.errai.ioc.rebind.JSR330AnnotationHandler;
import org.jboss.errai.ioc.rebind.ioc.ContextualProviderInjector;
import org.jboss.errai.ioc.rebind.ioc.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.IOCExtensionConfigurator;
import org.jboss.errai.ioc.rebind.ioc.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.InjectionFailure;
import org.jboss.errai.ioc.rebind.ioc.Injector;
import org.jboss.errai.ioc.rebind.ioc.InjectorFactory;
import org.jboss.errai.ioc.rebind.ioc.ProviderInjector;
import org.jboss.errai.ioc.rebind.ioc.QualifyingMetadataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * The main generator class for the Errai IOC system.
 *
 * @author Mike Brock <cbrock@redhat.com>
 */
public class IOCBootstrapGenerator {
  IOCProcessingContext procContext;
  TypeOracle typeOracle;
  GeneratorContext context;

  InjectorFactory injectFactory;
  IOCProcessorFactory procFactory;

  private List<String> packages = null;
  private boolean useReflectionStubs = false;
  private List<Runnable> deferredTasks = new ArrayList<Runnable>();

  private List<Class<?>> beforeTasks = new ArrayList<Class<?>>();
  private List<Class<?>> afterTasks = new ArrayList<Class<?>>();

  private Logger log = LoggerFactory.getLogger(IOCBootstrapGenerator.class);

  public static final String QUALIFYING_METADATA_FACTORY_PROPERTY = "errai.ioc.QualifyingMetaDataFactory";

  TreeLogger logger = new TreeLogger() {
    @Override
    public TreeLogger branch(Type type, String msg, Throwable caught, HelpInfo helpInfo) {
      return null;
    }

    @Override
    public boolean isLoggable(Type type) {
      return false;
    }

    @Override
    public void log(Type type, String msg, Throwable caught, HelpInfo helpInfo) {
      System.out.println(type.getLabel() + ": " + msg);
      if (caught != null) {
        caught.printStackTrace();
      }
    }
  };

  public IOCBootstrapGenerator(TypeOracle typeOracle,
                               GeneratorContext context,
                               TreeLogger logger) {
    this.typeOracle = typeOracle;
    this.context = context;
    this.logger = logger;
  }

  public IOCBootstrapGenerator(TypeOracle typeOracle,
                               GeneratorContext context,
                               TreeLogger logger,
                               List<String> packages) {
    this(typeOracle, context, logger);
    this.packages = packages;
  }

  public IOCBootstrapGenerator() {
  }

  public String generate(String packageName, String className) {
    File fileCacheDir = RebindUtils.getErraiCacheDir();
    File cacheFile = new File(fileCacheDir.getAbsolutePath() + "/" + className + ".java");

    final Set<Class<? extends Annotation>> annos = new HashSet<Class<? extends Annotation>>();
    annos.add(ApplicationScoped.class);
    annos.add(SessionScoped.class);
    annos.add(RequestScoped.class);
    annos.add(Singleton.class);
    annos.add(EntryPoint.class);
    annos.add(IOCBootstrapTask.class);

    String gen;
    if (RebindUtils.hasClasspathChangedForAnnotatedWith(annos) || !cacheFile.exists()) {
      log.info("generating IOC bootstrapping class...");
      long st = System.currentTimeMillis();
      gen = _generate(packageName, className);
      log.info("generated IOC bootstrapping class in " + (System.currentTimeMillis() - st) + "ms");

      RebindUtils.writeStringToFile(cacheFile, gen);
    }
    else {
      gen = RebindUtils.readFileToString(cacheFile);
      log.info("nothing has changed. using cached IOC bootstrapping class.");
    }

    log.info("using IOC bootstrapping code to: " + cacheFile.getAbsolutePath());

    return gen;
  }

  private String _generate(String packageName, String className) {
    ClassStructureBuilder<?> classStructureBuilder =
            Implementations.implement(Bootstrapper.class, packageName, className);

    BuildMetaClass bootStrapClass = classStructureBuilder.getClassDefinition();
    Context buildContext = bootStrapClass.getContext();

    BlockBuilder<?> blockBuilder =
            classStructureBuilder.publicMethod(InterfaceInjectionContext.class, "bootstrapContainer");

    SourceWriter sourceWriter = new StringSourceWriter();

    procContext = new IOCProcessingContext(logger, context, sourceWriter, buildContext, bootStrapClass, blockBuilder);
    injectFactory = new InjectorFactory(procContext);
    procFactory = new IOCProcessorFactory(injectFactory);

    MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();
    Properties props = scanner.getProperties("ErraiApp.properties");
    if (props != null) {
      logger.log(TreeLogger.Type.INFO, "Checking ErraiApp.properties for configured types ...");

      for (Object o : props.keySet()) {
        String key = (String) o;
        if (key.equals(QUALIFYING_METADATA_FACTORY_PROPERTY)) {
          String fqcnQualifyingMetadataFactory = String.valueOf(props.get(key));

          try {
            QualifyingMetadataFactory factory = (QualifyingMetadataFactory)
                    Class.forName
                            (fqcnQualifyingMetadataFactory).newInstance();

            procContext.setQualifyingMetadataFactory(factory);
          }
          catch (ClassNotFoundException e) {
            e.printStackTrace();
          }
          catch (InstantiationException e) {
            e.printStackTrace();
          }
          catch (IllegalAccessException e) {
            e.printStackTrace();
          }
        }
      }
    }

    procContext.setPackages(packages);

    defaultConfigureProcessor();

    // generator constructor source code
    initializeProviders();
    generateExtensions(sourceWriter, classStructureBuilder, blockBuilder);
    // close generated class

    return sourceWriter.toString();
  }

  private void generateExtensions(SourceWriter sourceWriter, ClassStructureBuilder<?> classBuilder,
                                  BlockBuilder<?> blockBuilder) {
    blockBuilder.append(
            Stmt.declareVariable(procContext.getContextVariableReference().getType()).asFinal()
                    .named(procContext.getContextVariableReference().getName())
                    .initializeWith(Stmt.newObject(InterfaceInjectionContext.class)));

    _doRunnableTasks(beforeTasks, blockBuilder);

    MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();

    procFactory.process(scanner, procContext);
   // procFactory.processAll();

    runAllDeferred();

    for (Statement stmt : procContext.getAppendToEnd()) {
      blockBuilder.append(stmt);
    }

    for (Statement stmt : procContext.getPostConstructStatements()) {
      blockBuilder.append(stmt);
    }



    Collection<MetaField> privateFields = injectFactory.getInjectionContext().getPrivateFieldsToExpose();
    for (MetaField f : privateFields) {
      GenUtil.addPrivateAccessStubs(!useReflectionStubs, classBuilder, f);
    }

    Collection<MetaMethod> privateMethods = injectFactory.getInjectionContext().getPrivateMethodsToExpose();

    for (MetaMethod m : privateMethods) {
      GenUtil.addPrivateAccessStubs(!useReflectionStubs, classBuilder, m);
    }

    _doRunnableTasks(afterTasks, blockBuilder);

    blockBuilder.append(Stmt.loadVariable(procContext.getContextVariableReference()).returnValue());

    blockBuilder.finish();

    sourceWriter.print(classBuilder.toJavaString());
  }

  private static void _doRunnableTasks(Collection<Class<?>> classes, BlockBuilder<?> blockBuilder) {
    for (Class<?> clazz : classes) {
      if (!Runnable.class.isAssignableFrom(clazz)) {
        throw new RuntimeException("annotated @IOCBootstrap task: " + clazz.getName() + " is not of type: "
                + Runnable.class.getName());
      }

      blockBuilder.append(Stmt.nestedCall(Stmt.newObject(clazz)).invoke("run"));
    }
  }

  public void addDeferred(Runnable task) {
    deferredTasks.add(task);
  }

  private void runAllDeferred() {
    injectFactory.getInjectionContext().runAllDeferred();

    for (Runnable r : deferredTasks)
      r.run();
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

  public void initializeProviders() {
    final MetaClass typeProviderCls = MetaClassFactory.get(TypeProvider.class);
    MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();

    /*
    * IOCDecoratorExtension.class
    */
    Set<Class<?>> iocExtensions = scanner
            .getTypesAnnotatedWith(org.jboss.errai.ioc.client.api.IOCExtension.class);
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

    Set<Class<?>> bootstrappers = scanner.getTypesAnnotatedWith(IOCBootstrapTask.class);
    for (Class<?> clazz : bootstrappers) {
      IOCBootstrapTask task = clazz.getAnnotation(IOCBootstrapTask.class);
      if (task.value() == TaskOrder.Before) {
        beforeTasks.add(clazz);
      }
      else {
        afterTasks.add(clazz);
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

          // noinspection unchecked
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
      MetaClass type = MetaClassFactory.get(clazz);

      boolean contextual = false;
      for (MetaClass iface : type.getInterfaces()) {
        if (iface.getFullyQualifiedName().equals(Provider.class.getName())) {
          injectFactory.addType(type);

          MetaParameterizedType pType = iface.getParameterizedType();
          MetaType typeParm = pType.getTypeParameters()[0];
          if (typeParm instanceof MetaParameterizedType) {
            bindType = (MetaClass) ((MetaParameterizedType) typeParm).getRawType();
          }
          else {
            bindType = (MetaClass) pType.getTypeParameters()[0];
          }

          boolean isContextual = false;
          for (MetaField field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)
                    && field.getType().isAssignableTo(ContextualProviderContext.class)) {

              isContextual = true;
              break;
            }
          }

          if (isContextual) {
            injectFactory.addInjector(new ContextualProviderInjector(bindType, type, procContext));
          }
          else {
            injectFactory.addInjector(new ProviderInjector(bindType, type, procContext));
          }
          break;
        }

        if (iface.getFullyQualifiedName().equals(ContextualTypeProvider.class.getName())) {
          contextual = true;

          MetaParameterizedType pType = iface.getParameterizedType();

          if (pType == null) {
            throw new InjectionFailure("could not determine the bind type for the IOCProvider class: "
                    + type.getFullyQualifiedName());
          }

          // todo: check for nested type parameters
          MetaType typeParm = pType.getTypeParameters()[0];
          if (typeParm instanceof MetaParameterizedType) {
            bindType = (MetaClass) ((MetaParameterizedType) typeParm).getRawType();
          }
          else {
            bindType = (MetaClass) pType.getTypeParameters()[0];
          }
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

          // todo: check for nested type parameters
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
        injector = new ContextualProviderInjector(finalBindType, type, procContext);
      }
      else {
        injector = new ProviderInjector(finalBindType, type, procContext);
      }

      injectFactory.addInjector(injector);
    }

    /**
     * GeneratedBy.class
     */
    Set<Class<?>> generatedBys = scanner.getTypesAnnotatedWith(GeneratedBy.class);
    for (Class<?> clazz : generatedBys) {
      MetaClass type = GWTClass.newInstance(typeOracle, clazz.getName());
      GeneratedBy anno = type.getAnnotation(GeneratedBy.class);
      Class<? extends ContextualTypeProvider> injectorClass = anno.value();

      try {
        injectFactory
                .addInjector(new ContextualProviderInjector(type, MetaClassFactory.get(injectorClass), procContext));
      }
      catch (Exception e) {
        throw new ErraiBootstrapFailure("could not load injector: " + e.getMessage(), e);
      }
    }

    for (IOCExtensionConfigurator extensionConfigurator : extensionConfigurators) {
      extensionConfigurator.afterInitialization(procContext, injectFactory, procFactory);
    }
  }

  private void defaultConfigureProcessor() {
    final MetaClass widgetType = MetaClassFactory.get(Widget.class);

    procContext.addSingletonScopeAnnotation(Singleton.class);
    procContext.addSingletonScopeAnnotation(EntryPoint.class);
    procContext.addSingletonScopeAnnotation(Service.class);

    procFactory.registerHandler(Singleton.class, new JSR330AnnotationHandler<Singleton>() {
      @Override
      public boolean handle(final InjectableInstance type, Singleton annotation, IOCProcessingContext context) {
        generateWithSingletonSemantics(type.getType());
        return true;
      }
    });

    procFactory.registerHandler(EntryPoint.class, new JSR330AnnotationHandler<EntryPoint>() {
      @Override
      public boolean handle(final InjectableInstance type, EntryPoint annotation, IOCProcessingContext context) {
        generateWithSingletonSemantics(type.getType());
        return true;
      }
    });

    procFactory.registerHandler(Service.class, new JSR330AnnotationHandler<Service>() {
      @Override
      public boolean handle(final InjectableInstance type, Service annotation, IOCProcessingContext context) {
        generateWithSingletonSemantics(type.getType());
        return true;
      }
    });

    procFactory.registerHandler(ToRootPanel.class, new JSR330AnnotationHandler<ToRootPanel>() {
      @Override
      public boolean handle(final InjectableInstance type, final ToRootPanel annotation,
                            final IOCProcessingContext context) {
        if (widgetType.isAssignableFrom(type.getType())) {

          addDeferred(new Runnable() {
            @Override
            public void run() {
              context.getWriter()
                      .println("ctx.addToRootPanel(" + generateWithSingletonSemantics(type.getType()) + ");");
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

    procFactory.registerHandler(CreatePanel.class, new JSR330AnnotationHandler<CreatePanel>() {
      @Override
      public boolean handle(final InjectableInstance type, final CreatePanel annotation,
                            final IOCProcessingContext context) {
        if (widgetType.isAssignableFrom(type.getType())) {

          addDeferred(new Runnable() {
            @Override
            public void run() {
              context.getWriter().println(
                      "ctx.registerPanel(\"" + (annotation.value().equals("")
                              ? type.getType().getName() : annotation.value()) + "\", " + generateInjectors(type.getType())
                              + ");");
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

    procFactory.registerHandler(ToPanel.class, new JSR330AnnotationHandler<ToPanel>() {
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

  public void setUseReflectionStubs(boolean useReflectionStubs) {
    this.useReflectionStubs = useReflectionStubs;
  }

  public void setPackages(List<String> packages) {
    this.packages = packages;
  }
}