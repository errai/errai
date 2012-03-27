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
import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.user.rebind.StringSourceWriter;
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
import org.jboss.errai.codegen.framework.meta.impl.gwt.GWTUtil;
import org.jboss.errai.codegen.framework.util.GenUtil;
import org.jboss.errai.codegen.framework.util.Implementations;
import org.jboss.errai.codegen.framework.util.PrivateAccessType;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.ioc.client.BootstrapperInjectionContext;
import org.jboss.errai.ioc.client.ContextualProviderContext;
import org.jboss.errai.ioc.client.api.Bootstrapper;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.api.IOCBootstrapTask;
import org.jboss.errai.ioc.client.api.IOCProvider;
import org.jboss.errai.ioc.client.api.TaskOrder;
import org.jboss.errai.ioc.client.api.TestMock;
import org.jboss.errai.ioc.client.api.TypeProvider;
import org.jboss.errai.ioc.client.container.CreationalContext;
import org.jboss.errai.ioc.rebind.ioc.exception.InjectionFailure;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCExtensionConfigurator;
import org.jboss.errai.ioc.rebind.ioc.injector.AbstractInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.ContextualProviderInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.ProviderInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Default;
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
import java.util.Map;
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

  InjectionContext injectionContext;
  IOCProcessorFactory procFactory;

  private Collection<String> packages = null;
  private boolean useReflectionStubs = false;

  private List<Class<?>> beforeTasks = new ArrayList<Class<?>>();
  private List<Class<?>> afterTasks = new ArrayList<Class<?>>();

  private Logger log = LoggerFactory.getLogger(IOCBootstrapGenerator.class);

  public static final String QUALIFYING_METADATA_FACTORY_PROPERTY = "errai.ioc.QualifyingMetaDataFactory";
  public static final String ENABLED_ALTERNATIVES_PROPERTY = "errai.ioc.enabled.alternatives";

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
                               Collection<String> packages) {
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
    annos.add(Dependent.class);
    annos.add(Default.class);

    String gen;

    if (context != null) {
      // context == null during some tests, in which case we don't have a GWT type oracle
      GWTUtil.populateMetaClassFactoryFromTypeOracle(context, logger);
    }

    log.info("generating IOC bootstrapping class...");
    long st = System.currentTimeMillis();
    gen = _generate(packageName, className);
    log.info("generated IOC bootstrapping class in " + (System.currentTimeMillis() - st) + "ms");

    RebindUtils.writeStringToFile(cacheFile, gen);

    log.info("using IOC bootstrapping code at: " + cacheFile.getAbsolutePath());

    return gen;
  }

  private String _generate(String packageName, String className) {
    ClassStructureBuilder<?> classStructureBuilder =
            Implementations.implement(Bootstrapper.class, packageName, className);

    logger.log(com.google.gwt.core.ext.TreeLogger.Type.DEBUG, "Generating IOC Bootstrapper " + packageName + "." + className);

    BuildMetaClass bootStrapClass = classStructureBuilder.getClassDefinition();
    Context buildContext = bootStrapClass.getContext();

    BlockBuilder<?> blockBuilder =
            classStructureBuilder.publicMethod(BootstrapperInjectionContext.class, "bootstrapContainer")
            .methodComment("The main IOC bootstrap method.");

    SourceWriter sourceWriter = new StringSourceWriter();

    procContext = new IOCProcessingContext(logger, context, sourceWriter, buildContext, bootStrapClass, blockBuilder);
    injectionContext = new InjectionContext(procContext);
    procFactory = new IOCProcessorFactory(injectionContext);

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
        else if (key.equals(ENABLED_ALTERNATIVES_PROPERTY)) {
          String[] alternatives = String.valueOf(props.get(ENABLED_ALTERNATIVES_PROPERTY)).split("\\s");
          for (String alternative : alternatives) {
            injectionContext.addEnabledAlternative(alternative.trim());
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
                    .initializeWith(Stmt.newObject(BootstrapperInjectionContext.class)));

    blockBuilder.append(Stmt.declareVariable(CreationalContext.class)
            .named("context")
            .initializeWith(Stmt.loadVariable(procContext.getContextVariableReference().getName())
                    .invoke("getRootContext")));

    _doRunnableTasks(beforeTasks, blockBuilder);

    MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();

    procFactory.process(scanner, procContext);

    for (Statement stmt : procContext.getAppendToEnd()) {
      blockBuilder.append(stmt);
    }

    Map<MetaField, PrivateAccessType> privateFields = injectionContext.getPrivateFieldsToExpose();
    for (Map.Entry<MetaField, PrivateAccessType> f : privateFields.entrySet()) {
      GenUtil.addPrivateAccessStubs(f.getValue(), !useReflectionStubs, classBuilder, f.getKey());
    }

    Collection<MetaMethod> privateMethods = injectionContext.getPrivateMethodsToExpose();

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

  public void initializeProviders() {
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

        configurator.configure(procContext, injectionContext, procFactory);

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

        injectionContext.registerDecorator(
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
          injectionContext.addType(type);

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
            if (injectionContext.isElementType(WiringElementType.InjectionPoint, field)
                    && field.getType().isAssignableTo(ContextualProviderContext.class)) {

              isContextual = true;
              break;
            }
          }

          if (isContextual) {
            injectionContext.registerInjector(new ContextualProviderInjector(bindType, type, injectionContext));
          }
          else {
            injectionContext.registerInjector(new ProviderInjector(bindType, type, injectionContext));
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
        throw new InjectionFailure("the annotated provider class does not appear to implement " +
                TypeProvider.class.getName() + ": " + type.getFullyQualifiedName());
      }

      final MetaClass finalBindType = bindType;

      AbstractInjector injector;
      if (contextual) {
        injector = new ContextualProviderInjector(finalBindType, type, injectionContext);
      }
      else {
        injector = new ProviderInjector(finalBindType, type, injectionContext);
      }

      injectionContext.registerInjector(injector);
    }


    for (IOCExtensionConfigurator extensionConfigurator : extensionConfigurators) {
      extensionConfigurator.afterInitialization(procContext, injectionContext, procFactory);
    }
  }

  private void defaultConfigureProcessor() {
    injectionContext.mapElementType(WiringElementType.SingletonBean, Singleton.class);
    injectionContext.mapElementType(WiringElementType.SingletonBean, EntryPoint.class);
    injectionContext.mapElementType(WiringElementType.SingletonBean, Service.class);

    injectionContext.mapElementType(WiringElementType.DependentBean, Dependent.class);

    injectionContext.mapElementType(WiringElementType.InjectionPoint, Inject.class);
    injectionContext.mapElementType(WiringElementType.InjectionPoint, com.google.inject.Inject.class);

    injectionContext.mapElementType(WiringElementType.AlternativeBean, Alternative.class);
    injectionContext.mapElementType(WiringElementType.TestMockBean, TestMock.class);
  }

  public void setUseReflectionStubs(boolean useReflectionStubs) {
    this.useReflectionStubs = useReflectionStubs;
  }

  public void setPackages(List<String> packages) {
    this.packages = packages;
  }
}