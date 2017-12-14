/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.rebind.ioc.bootstrapper;

import com.google.gwt.core.ext.GeneratorContext;
import jsinterop.annotations.JsType;
import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.BlockBuilderImpl;
import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaEnum;
import org.jboss.errai.codegen.util.Implementations;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.config.MetaClassFinder;
import org.jboss.errai.common.apt.generator.app.ResourceFilesFinder;
import org.jboss.errai.common.client.api.annotations.IOCProducer;
import org.jboss.errai.common.server.api.ErraiBootstrapFailure;
import org.jboss.errai.config.ErraiConfiguration;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.ioc.client.Bootstrapper;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.api.IOCBootstrapTask;
import org.jboss.errai.ioc.client.api.IOCExtension;
import org.jboss.errai.ioc.client.api.IOCProvider;
import org.jboss.errai.ioc.client.api.SharedSingleton;
import org.jboss.errai.ioc.client.api.TaskOrder;
import org.jboss.errai.ioc.client.container.ContextManager;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCExtensionConfigurator;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.jboss.errai.codegen.util.Stmt.loadVariable;

/**
 * Generator for the Bootstrapper class generated to wire an application at runtime.
 *
 * @author Mike Brock <cbrock@redhat.com>
 */
public class IOCBootstrapGenerator {
  private static final Logger log = LoggerFactory.getLogger(IOCBootstrapGenerator.class);

  private final MetaClassFinder metaClassFinder;
  private final GeneratorContext context;

  private final List<MetaClass> beforeTasks = new ArrayList<>();
  private final List<MetaClass> afterTasks = new ArrayList<>();

  private final ErraiConfiguration erraiConfiguration;
  private final IocRelevantClassesFinder relevantClassesFinder;
  private final ResourceFilesFinder resourceFilesFinder;


  private static final Object generatorLock = new Object();

  public IOCBootstrapGenerator(final MetaClassFinder metaClassFinder,
          final ResourceFilesFinder resourceFilesFinder,
          final GeneratorContext context,
          final ErraiConfiguration erraiConfiguration,
          final IocRelevantClassesFinder relevantClassesFinder) {

    this.resourceFilesFinder = resourceFilesFinder;
    this.metaClassFinder = metaClassFinder;
    this.context = context;
    this.erraiConfiguration = erraiConfiguration;
    this.relevantClassesFinder = relevantClassesFinder;
  }

  public String generate(final String packageName, final String className) {
    synchronized (generatorLock) {
      EnvUtil.recordEnvironmentState();

      log.info("generating IOC bootstrapping class...");
      final long st = System.currentTimeMillis();

      log.debug("setting up injection context...");
      final long injectionStart = System.currentTimeMillis();
      final InjectionContext injectionContext = buildInjectionContext(packageName, className);
      log.debug("injection context setup in " + (System.currentTimeMillis() - injectionStart) + "ms");

      final String gen = generateBootstrappingClassSource(injectionContext);
      log.info("generated IOC bootstrapping class in " + (System.currentTimeMillis() - st) + "ms ");

      return gen;
    }
  }

  private InjectionContext buildInjectionContext(final String packageName, final String className) {

    final ClassStructureBuilder<?> classStructureBuilder = Implementations.implement(Bootstrapper.class, packageName,
            className);

    log.debug("Generating IOC Bootstrapper " + packageName + "." + className);

    final Context buildContext = classStructureBuilder.getClassDefinition().getContext();
    buildContext.addInterningCallback(new BootstrapInterningCallback(classStructureBuilder, buildContext));

    final BlockBuilder<?> blockBuilder = classStructureBuilder.publicMethod(ContextManager.class, "bootstrapContainer")
            .methodComment("The main IOC bootstrap method.");

    final IOCProcessingContext iocProcessingContext = IOCProcessingContext.Builder.create()
            .blockBuilder(blockBuilder)
            .generatorContext(context)
            .metaClassFinder(metaClassFinder)
            .erraiConfiguration(erraiConfiguration)
            .resourceFilesFinder(resourceFilesFinder)
            .bootstrapClassInstance(classStructureBuilder.getClassDefinition())
            .bootstrapBuilder(classStructureBuilder)
            .build();

    return buildInjectionContext(iocProcessingContext);
  }

  private InjectionContext buildInjectionContext(final IOCProcessingContext iocProcessingContext) {

    final InjectionContext injectionContext = InjectionContext.Builder.create()
            .processingContext(iocProcessingContext)
            .enabledAlternatives(classNames(erraiConfiguration.modules().getIocEnabledAlternatives()))
            .addToWhitelist(classNames(erraiConfiguration.modules().getIocWhitelist()))
            .addToBlacklist(classNames(erraiConfiguration.modules().getIocBlacklist()))
            .asyncBootstrap(erraiConfiguration.app().asyncBeanManager())
            .build();

    injectionContext.mapElementType(WiringElementType.PseudoScopedBean, Singleton.class);
    injectionContext.mapElementType(WiringElementType.NormalScopedBean, ApplicationScoped.class);
    injectionContext.mapElementType(WiringElementType.NormalScopedBean, SharedSingleton.class);
    injectionContext.mapElementType(WiringElementType.PseudoScopedBean, EntryPoint.class);
    injectionContext.mapElementType(WiringElementType.ProducerElement, IOCProducer.class);
    injectionContext.mapElementType(WiringElementType.DependentBean, Dependent.class);
    injectionContext.mapElementType(WiringElementType.Provider, IOCProvider.class);
    injectionContext.mapElementType(WiringElementType.InjectionPoint, Inject.class);
    injectionContext.mapElementType(WiringElementType.InjectionPoint, com.google.inject.Inject.class);
    injectionContext.mapElementType(WiringElementType.AlternativeBean, Alternative.class);

    return injectionContext;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private String generateBootstrappingClassSource(final InjectionContext injectionContext) {

    log.debug("Processing IOC extensions...");
    long start = System.currentTimeMillis();
    processExtensions(injectionContext);
    log.debug("Extensions processed in {}ms", (System.currentTimeMillis() - start));

    final IOCProcessor iocProcessor = new IOCProcessor(injectionContext, erraiConfiguration);
    final IOCProcessingContext iocProcessingContext = injectionContext.getProcessingContext();
    final ClassStructureBuilder<?> classBuilder = iocProcessingContext.getBootstrapBuilder();
    final BlockBuilder<?> blockBuilder = iocProcessingContext.getBlockBuilder();
    final BlockBuilder builder = new BlockBuilderImpl(classBuilder.getClassDefinition().getInstanceInitializer(), null);

    doBeforeRunnables(builder);

    log.debug("Process dependency graph...");
    start = System.currentTimeMillis();

    iocProcessor.process(iocProcessingContext, relevantClassesFinder.find(getRelevantAnnotations(injectionContext)));
    log.debug("Processed dependency graph in {}ms", System.currentTimeMillis() - start);

    doAfterRunnbales(blockBuilder);

    blockBuilder.append(loadVariable("contextManager").returnValue());
    blockBuilder.finish();

    start = System.currentTimeMillis();
    final String bootstrapperImplString = classBuilder.toJavaString();
    log.debug("Generated BootstrapperImpl String in {}ms", System.currentTimeMillis() - start);

    return bootstrapperImplString;
  }

  private Collection<Class<? extends Annotation>> getRelevantAnnotations(final InjectionContext injectionContext) {
    final Collection<Class<? extends Annotation>> annotations = new ArrayList<>(injectionContext.getAllElementBindingRegisteredAnnotations());
    annotations.add(JsType.class);
    return annotations;
  }

  private void doAfterRunnbales(final BlockBuilder<?> blockBuilder) {
    long start;
    log.debug("Running after tasks...");
    start = System.currentTimeMillis();
    doRunnableTasks(afterTasks, blockBuilder);
    log.debug("Tasks run in " + (System.currentTimeMillis() - start) + "ms");
  }

  private void doBeforeRunnables(final BlockBuilder<?> builder) {
    long start;
    log.debug("Running before tasks...");
    start = System.currentTimeMillis();
    doRunnableTasks(beforeTasks, builder);
    log.debug("Tasks run in " + (System.currentTimeMillis() - start) + "ms");
  }

  private void doRunnableTasks(final Collection<MetaClass> classes, final BlockBuilder<?> blockBuilder) {
    for (final MetaClass clazz : classes) {
      if (!clazz.isAssignableTo(Runnable.class)) {
        throw new RuntimeException("annotated @IOCBootstrap task: " + clazz.getName() + " is not of type: "
                + Runnable.class.getName());
      }

      blockBuilder.append(Stmt.nestedCall(Stmt.newObject(clazz)).invoke("run"));
    }
  }

  @SuppressWarnings("rawtypes")
  private void processExtensions(final InjectionContext injectionContext) {

    //Configure IocExtensions
    final List<IOCExtensionConfigurator> extensionConfigurators = metaClassFinder.findAnnotatedWith(IOCExtension.class)
            .stream()
            .map(this::newIocExtension)
            .peek(extension -> extension.configure(injectionContext.getProcessingContext(), injectionContext))
            .collect(toList());

    //Configure IocBootstrapTasks
    for (final MetaClass metaClass : metaClassFinder.findAnnotatedWith(IOCBootstrapTask.class)) {
      final MetaAnnotation task = metaClass.getAnnotation(IOCBootstrapTask.class).get();
      final TaskOrder taskOrder = task.<MetaEnum>value().as(TaskOrder.class);
      if (taskOrder.equals(TaskOrder.Before)) {
        beforeTasks.add(metaClass);
      } else {
        afterTasks.add(metaClass);
      }
    }

    //Configure CodeDecorators
    metaClassFinder.findAnnotatedWith(CodeDecorator.class).forEach(c -> {
      final Class<? extends IOCDecoratorExtension> decoratorClass = getIocDecoratorClass(c);
      final Class<? extends Annotation> annotationType = getAnnotationType(decoratorClass);
      injectionContext.registerDecorator(getIocExtension(decoratorClass, annotationType));
    });

    extensionConfigurators.forEach(extensionConfigurator -> {
      extensionConfigurator.afterInitialization(injectionContext.getProcessingContext(), injectionContext);
    });
  }

  private IOCDecoratorExtension<?> getIocExtension(final Class<? extends IOCDecoratorExtension> key,
          final Class<? extends Annotation> value) {

    try {
      return key.getConstructor(new Class[] { Class.class }).newInstance(value);
    } catch (Exception e) {
      throw new ErraiBootstrapFailure("unable to load code decorator: " + e.getMessage(), e);
    }
  }

  private Class<? extends Annotation> getAnnotationType(final Class<? extends IOCDecoratorExtension> decoratorClass) {

    Class<? extends Annotation> annoType = null;
    final Type t = decoratorClass.getGenericSuperclass();
    if (!(t instanceof ParameterizedType)) {
      throw new ErraiBootstrapFailure("code decorator must extend IOCDecoratorExtension<@AnnotationType>");
    }

    final ParameterizedType pType = (ParameterizedType) t;
    if (IOCDecoratorExtension.class.equals(pType.getRawType())) {
      if (pType.getActualTypeArguments().length == 0
              || !Annotation.class.isAssignableFrom((Class<?>) pType.getActualTypeArguments()[0])) {
        throw new ErraiBootstrapFailure("code decorator must extend IOCDecoratorExtension<@AnnotationType>");
      }

      // noinspection unchecked
      annoType = ((Class<?>) pType.getActualTypeArguments()[0]).asSubclass(Annotation.class);
    }
    return annoType;
  }

  private IOCExtensionConfigurator newIocExtension(final MetaClass metaClass) {
    try {
      // Because we're sure all IOCExtensions will be precompiled when generating code for an @ErraiApp,
      // it's safe to run a Class.forName on the APT environment too
      return Class.forName(metaClass.getCanonicalName()).asSubclass(IOCExtensionConfigurator.class).newInstance();
    } catch (final Exception e) {
      throw new ErraiBootstrapFailure("unable to load IOC Extension Configurator: " + e.getMessage(), e);
    }
  }

  private Class<? extends IOCDecoratorExtension> getIocDecoratorClass(final MetaClass metaClass) {
    try {
      // Because we're sure all IOCDecoratorExtensions will be precompiled when generating code for an @ErraiApp,
      // it's safe to run a Class.forName on the APT environment too
      return Class.forName(metaClass.getCanonicalName()).asSubclass(IOCDecoratorExtension.class);
    } catch (final Exception e) {
      throw new ErraiBootstrapFailure("unable to load IOC Extension Configurator: " + e.getMessage(), e);
    }
  }

  private Collection<String> classNames(final Set<MetaClass> metaClasses) {
    return metaClasses.stream().map(MetaClass::getCanonicalName).collect(toList());
  }
}
