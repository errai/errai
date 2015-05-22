/*
 * Copyright 2013 JBoss, by Red Hat, Inc
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

import static org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance.getInjectedInstance;
import static org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance.getMethodInjectedInstance;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.*;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.NormalScope;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Stereotype;
import javax.inject.Provider;
import javax.inject.Scope;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaClassMember;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.config.util.ClassScanner;
import org.jboss.errai.config.util.ThreadUtil;
import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.EnabledByProperty;
import org.jboss.errai.ioc.client.api.TestMock;
import org.jboss.errai.ioc.client.api.TestOnly;
import org.jboss.errai.ioc.rebind.ioc.extension.AnnotationHandler;
import org.jboss.errai.ioc.rebind.ioc.extension.DependencyControl;
import org.jboss.errai.ioc.rebind.ioc.extension.JSR330AnnotationHandler;
import org.jboss.errai.ioc.rebind.ioc.extension.Rule;
import org.jboss.errai.ioc.rebind.ioc.extension.RuleDef;
import org.jboss.errai.ioc.rebind.ioc.graph.Dependency;
import org.jboss.errai.ioc.rebind.ioc.graph.GraphBuilder;
import org.jboss.errai.ioc.rebind.ioc.graph.GraphSort;
import org.jboss.errai.ioc.rebind.ioc.graph.SortUnit;
import org.jboss.errai.ioc.rebind.ioc.injector.Injector;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;

import com.google.gwt.core.ext.TreeLogger.Type;

/**
 * The main IOC configuration processor. This class is responsible for scanning the classpath, finding beans,
 * reading configuration, and then configuring the IOC code generator to emit the bootstrapper code.
 */
@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
public class IOCConfigProcessor {
  private final Stack<SortedSet<ProcessingEntry>> processingTasksStack = new Stack<SortedSet<ProcessingEntry>>();
  private final InjectionContext injectionContext;
  private final Set<String> visitedAutoDiscoveredDependentBeans = new HashSet<String>();

  public IOCConfigProcessor(final InjectionContext injectionContext) {
    this.injectionContext = injectionContext;
  }

  public void registerHandler(final Class<? extends Annotation> annotation,
                              final AnnotationHandler handler) {
    registerHandler(annotation, handler, null);
  }

  public void registerHandler(final Class<? extends Annotation> annotation,
                              final AnnotationHandler handler,
                              final List<RuleDef> rules) {

    getProcessingTasksSet().add(new ProcessingEntry(annotation, handler, rules));
  }

  protected SortedSet<ProcessingEntry> getProcessingTasksSet() {
    if (processingTasksStack.isEmpty()) {
      processingTasksStack.push(new TreeSet<ProcessingEntry>());
    }
    return processingTasksStack.peek();
  }

  class DependencyControlImpl implements DependencyControl {
    MetaClass masqueradeClass;
    Stack<SortedSet<ProcessingEntry>> tasksStack;

    DependencyControlImpl(final Stack<SortedSet<ProcessingEntry>> tasksStack) {
      this.tasksStack = tasksStack;
    }

    @Override
    public void masqueradeAs(final MetaClass clazz) {
      masqueradeClass = clazz;
    }

    @Override
    public void notifyDependency(final MetaClass dependentClazz) {
      if (injectionContext.isAnyKnownElementType(dependentClazz)) {
        injectionContext.getGraphBuilder().addDependency(masqueradeClass, Dependency.on(dependentClazz));
      }
      else {
        final DependencyControl control = new DependencyControl() {
          @Override
          public void masqueradeAs(final MetaClass clazz) {
            // can't masquerade.
          }

          @Override
          public void notifyDependency(final MetaClass clazz) {
            if (visitedAutoDiscoveredDependentBeans.contains(clazz.getFullyQualifiedName())) return;
            visitedAutoDiscoveredDependentBeans.add(clazz.getFullyQualifiedName());

            injectionContext.getGraphBuilder().addDependency(dependentClazz, Dependency.on(clazz));
          }

          @Override
          public void notifyDependencies(final Collection<MetaClass> classes) {
            for (final MetaClass clazz : classes) {
              notifyDependency(clazz);
            }
          }
        };

        injectionContext.getGraphBuilder().addDependency(masqueradeClass, Dependency.on(dependentClazz));
        JSR330AnnotationHandler.processDependencies(control, dependentClazz, injectionContext);
      }
    }

    @Override
    public void notifyDependencies(final Collection<MetaClass> classes) {
      for (final MetaClass clazz : classes) {
        notifyDependency(clazz);
      }
    }
  }

  private static boolean checkIfEnabled(MetaClass clazzType) {
    if (clazzType.isAnnotationPresent(EnabledByProperty.class)) {
      final EnabledByProperty enabledByProperty = clazzType.getAnnotation(EnabledByProperty.class);
      final String propertyValue = enabledByProperty.value();
      final boolean negatedTest = enabledByProperty.negated();

      boolean bool = Boolean.parseBoolean(EnvUtil.getEnvironmentConfig().getFrameworkOrSystemProperty(propertyValue));
      if (negatedTest) {
        bool = !bool;
      }
      if (!bool) {
        return false;
      }
    }
    return true;
  }

  @SuppressWarnings("unchecked")
  private void setupHandlers() {
    for (final Map.Entry<WiringElementType, Class<? extends Annotation>> entry
        : injectionContext.getAllElementMappings()) {
      switch (entry.getKey()) {
        case TopLevelProvider:
          registerHandler(entry.getValue(), new JSR330AnnotationHandler() {
            @SuppressWarnings("unchecked")
            @Override
            public void getDependencies(final DependencyControl control,
                                        final InjectableInstance instance,
                                        final Annotation annotation,
                                        final IOCProcessingContext context) {

              final MetaClass providerClassType = instance.getEnclosingType();

              if (!checkIfEnabled(providerClassType)) {
                return;
              }


              final MetaClass MC_Provider = MetaClassFactory.get(Provider.class);
              final MetaClass MC_ContextualTypeProvider = MetaClassFactory.get(ContextualTypeProvider.class);

              MetaClass providerInterface = null;
              final MetaClass providedType;

              if (MC_Provider.isAssignableFrom(providerClassType)) {
                for (final MetaClass interfaceType : providerClassType.getInterfaces()) {
                  if (MC_Provider.equals(interfaceType.getErased())) {
                    providerInterface = interfaceType;
                  }
                }

                if (providerInterface == null) {
                  throw new RuntimeException("top level provider " + providerClassType.getFullyQualifiedName()
                      + " must directly implement " + Provider.class.getName());
                }

                if (providerInterface.getParameterizedType() == null) {
                  throw new RuntimeException("top level provider " + providerClassType.getFullyQualifiedName()
                      + " must use a parameterized " + Provider.class.getName() + " interface type.");
                }

                final MetaType parmType = providerInterface.getParameterizedType().getTypeParameters()[0];
                if (parmType instanceof MetaParameterizedType) {
                  providedType = (MetaClass) ((MetaParameterizedType) parmType).getRawType();
                }
                else {
                  providedType = (MetaClass) parmType;
                }

                injectionContext.registerInjector(
                    injectionContext.getInjectorFactory().getProviderInjector(providedType, providerClassType, injectionContext)
                );
              }
              else if (MC_ContextualTypeProvider.isAssignableFrom(providerClassType)) {
                for (final MetaClass interfaceType : providerClassType.getInterfaces()) {
                  if (MC_ContextualTypeProvider.equals(interfaceType.getErased())) {
                    providerInterface = interfaceType;
                  }
                }

                if (providerInterface == null) {
                  throw new RuntimeException("top level provider " + providerClassType.getFullyQualifiedName()
                      + " must directly implement " + ContextualTypeProvider.class.getName());
                }

                if (providerInterface.getParameterizedType() == null) {
                  throw new RuntimeException("top level provider " + providerClassType.getFullyQualifiedName()
                      + " must use a parameterized " + ContextualTypeProvider.class.getName() + " interface type.");
                }

                final MetaType parmType = providerInterface.getParameterizedType().getTypeParameters()[0];
                if (parmType instanceof MetaParameterizedType) {
                  providedType = (MetaClass) ((MetaParameterizedType) parmType).getRawType();
                }
                else {
                  providedType = (MetaClass) parmType;
                }

                injectionContext.registerInjector(
                    injectionContext.getInjectorFactory()
                        .getContextualProviderInjector(providedType, providerClassType, injectionContext)
                );
              }
              else {
                throw new RuntimeException("top level provider " + providerClassType.getFullyQualifiedName()
                    + " does not implement: " + Provider.class.getName() + " or " + ContextualTypeProvider.class);
              }

              injectionContext.getGraphBuilder().addDependency(providedType, Dependency.on(providerClassType));

              control.masqueradeAs(providedType);
              super.getDependencies(control, instance, annotation, context);
            }

            @Override
            public boolean handle(final InjectableInstance instance,
                                  final Annotation annotation,
                                  final IOCProcessingContext context) {
              final List<Injector> injectors
                  = injectionContext.getInjectors(instance.getElementTypeOrMethodReturnType());
              for (final Injector injector : injectors) {
                if (injector.isEnabled() && injectionContext.isTypeInjectable(injector.getEnclosingType())) {
                  injector.renderProvider(instance);
                }
              }
              return true;
            }
          }, Rule.before(injectionContext.getAnnotationsForElementType(WiringElementType.SingletonBean),
              injectionContext.getAnnotationsForElementType(WiringElementType.DependentBean)));

          break;
      }
    }

    for (final Map.Entry<WiringElementType, Class<? extends Annotation>> entry
        : injectionContext.getAllElementMappings()) {
      switch (entry.getKey()) {
        case ProducerElement:
          registerHandler(entry.getValue(), new JSR330AnnotationHandler() {
            @Override
            public void getDependencies(final DependencyControl control,
                                        final InjectableInstance instance,
                                        final Annotation annotation,
                                        final IOCProcessingContext context) {

              if (!checkIfEnabled(instance.getEnclosingType())) {
                return;
              }

              final MetaClass injectedType = instance.getElementTypeOrMethodReturnType();
              injectionContext.addTopLevelType(injectedType);

              control.masqueradeAs(injectedType);

              final MetaClassMember producerMember;

              control.masqueradeAs(injectedType);

              switch (instance.getTaskType()) {
                case PrivateMethod:
                case Method:
                  producerMember = instance.getMethod();

                  for (final MetaParameter parm : instance.getMethod().getParameters()) {
                    control.notifyDependency(parm.getType());
                    final Set<MetaClass> interfaceTypes = fillInInterface(parm.getType(), context.getGeneratorContext());
                    control.notifyDependencies(interfaceTypes);

                    if (!producerMember.isStatic()) {
                      final GraphBuilder graphBuilder = injectionContext.getGraphBuilder();
                      graphBuilder.addDependency(producerMember.getDeclaringClass(), Dependency.on(parm.getType()));
                      for (MetaClass type : interfaceTypes) {
                        graphBuilder.addDependency(producerMember.getDeclaringClass(), Dependency.on(type));
                      }
                    }
                  }

                  break;
                case PrivateField:
                case Field:
                  producerMember = instance.getField();
                  break;
                default:
                  throw new RuntimeException("illegal producer type");
              }

              final Injector producerInjector = injectionContext.getInjectorFactory()
                  .getProducerInjector(injectedType, producerMember, instance);

              injectionContext.registerInjector(producerInjector);


              if (!producerMember.isStatic()) {
                // if this is a static producer, it does not have a dependency on its parent bean
                injectionContext.getGraphBuilder()
                    .addDependency(injectedType, Dependency.on(instance.getEnclosingType()));
              }
            }

            @Override
            public boolean handle(final InjectableInstance instance,
                                  final Annotation annotation,
                                  final IOCProcessingContext context) {
              final List<Injector> injectors
                  = injectionContext.getInjectors(instance.getElementTypeOrMethodReturnType());
              for (final Injector injector : injectors) {
                if (injector.isEnabled()) {
                  injector.renderProvider(instance);
                }
              }
              return true;
            }

          }, Rule.after(injectionContext.getAnnotationsForElementType(WiringElementType.SingletonBean),
              injectionContext.getAnnotationsForElementType(WiringElementType.DependentBean)));
          break;

        case DependentBean:
          registerHandler(entry.getValue(), new JSR330AnnotationHandler() {
            @Override
            public boolean handle(final InjectableInstance instance,
                                  final Annotation annotation,
                                  final IOCProcessingContext context) {

              final List<Injector> injectors
                  = new ArrayList<Injector>(injectionContext.getInjectors(instance.getEnclosingType()));
              final Injector injector;

              final Iterator<Injector> injectorIterator = injectors.iterator();
              boolean removed = false;
              while (injectorIterator.hasNext()) {
                if (!injectorIterator.next().isEnabled()) {
                  injectorIterator.remove();
                  removed = true;
                }
              }

              if (injectors.isEmpty() && removed) {
                return true;
              }

              if (!injectors.isEmpty()) {
                injector = injectors.get(0);
              }
              else {
                injector = injectionContext.getInjectorFactory()
                    .getTypeInjector(instance.getEnclosingType(), injectionContext);
              }

              if (injector.isEnabled() && injector.isRegularTypeInjector()) {
                injector.renderProvider(instance);
              }
              return true;
            }
          });
          break;

        case SingletonBean:
          registerHandler(entry.getValue(), new JSR330AnnotationHandler() {
            @Override
            public boolean handle(final InjectableInstance instance,
                                  final Annotation annotation,
                                  final IOCProcessingContext context) {
              final Injector injector = injectionContext.getInjector(instance.getEnclosingType());

              if (injector.isEnabled()) {
                injector.renderProvider(instance);
              }
              return true;
            }
          });
          break;
      }
    }
  }

  @SuppressWarnings({"unchecked"})
  public void process(final IOCProcessingContext context) {
    setupHandlers();

    /**
     * Let's accumulate all the processing tasks.
     */
    do {
      for (final ProcessingEntry entry : processingTasksStack.pop()) {
        final Class<? extends Annotation> annotationClass = entry.annotationClass;
        Target target = annotationClass.getAnnotation(Target.class);

        if (target == null) {
          target = new Target() {
            @Override
            public ElementType[] value() {
              return new ElementType[]
                  {ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.FIELD,
                      ElementType.METHOD, ElementType.FIELD};
            }

            @Override
            public Class<? extends Annotation> annotationType() {
              return Target.class;
            }
          };
        }

        for (final ElementType elementType : target.value()) {
          final DependencyControlImpl dependencyControl = new DependencyControlImpl(processingTasksStack);

          switch (elementType) {
            case TYPE: {
              Collection<MetaClass> classes;

              classes = ClassScanner.getTypesAnnotatedWith(annotationClass, context.getPackages(), context.getGeneratorContext());
              
              // Get producer classes which are implicitly dependent
              if (annotationClass.equals(Dependent.class)) {
                classes = new ArrayList<MetaClass>(classes);
                final Collection<MetaClass> toCheck = new ArrayList<MetaClass>();
                for (final MetaMethod method : ClassScanner.getMethodsAnnotatedWith(Produces.class, context.getPackages(), 
                    context.getGeneratorContext())) {
                  toCheck.add(method.getDeclaringClass());
                }
                for (final MetaField field : ClassScanner.getFieldsAnnotatedWith(Produces.class, context.getPackages(), 
                    context.getGeneratorContext())) {
                  toCheck.add(field.getDeclaringClass());
                }
                
                for (final MetaClass clazz : toCheck) {
                  boolean scoped = false;
                  for (final Annotation anno : clazz.getAnnotations()) {
                    if (anno.annotationType().isAnnotationPresent(Scope.class)
                            || anno.annotationType().isAnnotationPresent(NormalScope.class)) {
                      scoped = true;
                      break;
                    }
                  }
                  if (!scoped) {
                    classes.add(clazz);
                  }
                }
              }

              if (annotationClass.equals(Dependent.class)
                  && Boolean.getBoolean(IOCBootstrapGenerator.EXPERIMENTAL_INFER_DEPENDENT_BY_REACHABILITY)
                  && injectionContext.getAllReachableTypes() != null) {

                System.out.println("******************************************************************************");
                System.out.println("*** EXPERIMENTAL FEATURE ENABLED                                           ***");
                System.out.println("*** You have enabled support for inferred dependent scope by reachability. ***");
                System.out.println("***                                                                        ***");
                System.out.println("*** This feature is designed to allow the @Dependent scope to work         ***");
                System.out.println("*** as per the JSR-299 specification, without imposing the overhead of     ***");
                System.out.println("*** adding all translatable beans to the container -- but rather, just     ***");
                System.out.println("*** those which are reachable within your application.                     ***");
                System.out.println("***                                                                        ***");
                System.out.println("*** This feature is only experimental, and may be removed in a future      ***");
                System.out.println("*** version without notice.                                                ***");
                System.out.println("******************************************************************************");

                classes = new ArrayList<MetaClass>(classes);

                MetaClass metaClass;
                Scan:
                for (final String type : injectionContext.getAllReachableTypes()) {
                  if (type.indexOf('$') != -1) {
                    try {
                      metaClass = MetaClassFactory.get(type);
                    }
                    catch (Throwable t) {
                      continue;
                    }
                  }
                  else {
                    metaClass = MetaClassFactory.get(type);
                  }

                  if (metaClass.isDefaultInstantiable() && metaClass.isPublic() && metaClass.isConcrete()) {
                    for (final Annotation anno : metaClass.getAnnotations()) {
                      if (anno.annotationType().isAnnotationPresent(Scope.class)
                          || anno.annotationType().isAnnotationPresent(NormalScope.class)) {
                        continue Scan;
                      }
                    }

                    classes.add(metaClass);
                  }
                }

                classes = Collections.unmodifiableCollection(classes);
              }

              injectionContext.addTopLevelTypes(classes);

              for (final MetaClass clazz : classes) {
                if (clazz.isAnnotation()) {
                  if (clazz.isAnnotationPresent(Stereotype.class)) {
                    final Class<? extends Annotation> stereoType = clazz.asClass().asSubclass(Annotation.class);
                    for (final MetaClass stereoTypedClass :
                        ClassScanner.getTypesAnnotatedWith(stereoType, context.getGeneratorContext())) {
                      handleType(entry, dependencyControl, stereoTypedClass, annotationClass, context);
                    }
                  }

                  //TODO: recurse and handle stereotypes
                  continue;
                }

                handleType(entry, dependencyControl, clazz, annotationClass, context);
              }
            }
            break;

            case METHOD: {
              for (final MetaMethod method : ClassScanner.getMethodsAnnotatedWith(annotationClass, context.getPackages(), 
                  context.getGeneratorContext())) {
                handleMethod(entry, dependencyControl, method, annotationClass, context);
              }
            }
            break;

            case FIELD: {
              for (final MetaField field : ClassScanner.getFieldsAnnotatedWith(annotationClass, context.getPackages(), 
                  context.getGeneratorContext())) {
                handleField(entry, dependencyControl, field, annotationClass, context);
              }
            }
          }
        }
      }
    }
    while (!processingTasksStack.isEmpty());

    final List<SortUnit> toSort = injectionContext.getGraphBuilder().build();
    final List<SortUnit> list = GraphSort.sortGraph(toSort);

    ThreadUtil.execute(new Runnable() {
      @Override
      public void run() {
        final File dotFile = new File(RebindUtils.getErraiCacheDir().getAbsolutePath() + "/beangraph.gv");
        RebindUtils.writeStringToFile(dotFile,
            "//\n" +
                "// Generated IOC bean dependency graph in GraphViz DOT format.\n" +
                "//\n\n" +
                GraphBuilder.toDOTRepresentation(list));
      }
    });


    for (final SortUnit unit : list) {
      if (unit.isCyclicGraph()) {
        final Set<String> knownCycles = new HashSet<String>();
        knownCycles.add(unit.getType().getFullyQualifiedName());

        for (final SortUnit dep : unit.getDependencies()) {
          if (dep.isCyclicGraph()) {
            knownCycles.add(dep.getType().getFullyQualifiedName());
          }
        }

        injectionContext.addKnownTypesWithCycles(knownCycles);
      }
    }

    for (final SortUnit unit : list) {
      for (final Object item : unit.getItems()) {
        if (item instanceof ProcessingDelegate) {
          ((ProcessingDelegate) item).process();
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void handleType(final ProcessingEntry entry,
                          final DependencyControl dependencyControl,
                          final MetaClass type,
                          final Class<? extends Annotation> aClass,
                          final IOCProcessingContext context) {

    if (type.getFullyQualifiedName().equals(Object.class.getName())) {
      return;
    }

    final Annotation annotation = type.getAnnotation(aClass);

    dependencyControl.masqueradeAs(type);

    if (!IOCGenerator.isTestMode) {
      if (type.isAnnotationPresent(TestOnly.class) || type.isAnnotationPresent(TestMock.class)) {
        context.treeLogger.log(Type.DEBUG, "Skipping test-only type " + type.getFullyQualifiedName());
        return;
      }
    }
    final InjectableInstance injectableInstance
        = getInjectedInstance(annotation, type, null, injectionContext);

    final ProcessingDelegate del = new ProcessingDelegate() {
      @Override
      public void processDependencies() {
        entry.handler.getDependencies(dependencyControl, injectableInstance, annotation, context);
      }

      @Override
      public boolean process() {
        if (!checkIfEnabled(type)) {
          return false;
        }

        injectionContext.addType(type);

        final List<Injector> injectors = injectionContext.getInjectors(type);
        final Injector injector;

        if (!injectors.isEmpty()) {
          injector = injectors.get(0);
        }
        else {
          injector = injectionContext.getInjectorFactory().getTypeInjector(type, injectionContext);
        }

        return entry.handler
            .handle(getInjectedInstance(annotation, type, injector, injectionContext), annotation, context);
      }

      @Override
      public String toString() {
        return type.getFullyQualifiedName();
      }
    };

    entry.handler.registerMetadata(injectableInstance, annotation, context);

    del.processDependencies();

    final MetaClass masqueradeClass = ((DependencyControlImpl) dependencyControl).masqueradeClass;

    injectionContext.getGraphBuilder().addItem(masqueradeClass, del);
  }

  @SuppressWarnings("unchecked")
  private void handleMethod(final ProcessingEntry entry,
                            final DependencyControl dependencyControl,
                            final MetaMethod metaMethod,
                            final Class<? extends Annotation> annotationClass,
                            final IOCProcessingContext context) {

    final Annotation annotation = metaMethod.getAnnotation(annotationClass);
    final MetaClass type = metaMethod.getDeclaringClass();

    //final MetaMethod metaMethod = MetaClassFactory.get(method);

    dependencyControl.masqueradeAs(type);

    final InjectableInstance injectableInstance
        = getMethodInjectedInstance(metaMethod, null,
        injectionContext);

    final ProcessingDelegate del = new ProcessingDelegate() {
      @Override
      public void processDependencies() {
        entry.handler.getDependencies(dependencyControl, injectableInstance, annotation, context);
      }

      @Override
      public boolean process() {
        if (!checkIfEnabled(type)) {
          return false;
        }

        injectionContext.addType(type);

        final Injector injector = injectionContext.getInjector(type);
        final InjectableInstance injectableInstance
            = getMethodInjectedInstance(metaMethod, injector,
            injectionContext);

        return entry.handler.handle(injectableInstance, annotation, context);
      }

      @Override
      public String toString() {
        return type.getFullyQualifiedName();
      }
    };

    entry.handler.registerMetadata(injectableInstance, annotation, context);

    del.processDependencies();

    final MetaClass masqueradeClass = ((DependencyControlImpl) dependencyControl).masqueradeClass;

    injectionContext.getGraphBuilder().addItem(masqueradeClass, del);
  }

  private void handleField(final ProcessingEntry entry,
                           final DependencyControl dependencyControl,
                           final MetaField metaField,
                           final Class<? extends Annotation> annotationClass,
                           final IOCProcessingContext context) {

    final Annotation annotation = metaField.getAnnotation(annotationClass);
    final MetaClass type = metaField.getDeclaringClass();

    dependencyControl.masqueradeAs(type);

    final ProcessingDelegate del = new ProcessingDelegate() {
      @SuppressWarnings("unchecked")
      @Override
      public void processDependencies() {

        entry.handler.getDependencies(dependencyControl, InjectableInstance.getFieldInjectedInstance(metaField, null,
            injectionContext), annotation, context);
      }

      @SuppressWarnings("unchecked")
      @Override
      public boolean process() {
        if (!checkIfEnabled(type)) {
          return false;
        }

        injectionContext.addType(type);

        final InjectableInstance injectableInstance
            = InjectableInstance.getFieldInjectedInstance(metaField, injectionContext.getInjector(type),
            injectionContext);

        entry.handler.registerMetadata(injectableInstance, annotation, context);

        return entry.handler.handle(injectableInstance, annotation, context);
      }

      @Override
      public String toString() {
        return type.getFullyQualifiedName();
      }
    };

    del.processDependencies();

    injectionContext.getGraphBuilder().addItem(((DependencyControlImpl) dependencyControl).masqueradeClass, del);
  }

  private class ProcessingEntry implements Comparable<ProcessingEntry> {
    private final Class<? extends Annotation> annotationClass;
    private final AnnotationHandler handler;
    private Set<RuleDef> rules;

    private ProcessingEntry(final Class<? extends Annotation> annotationClass,
                            final AnnotationHandler handler,
                            final List<RuleDef> rule) {

      this.annotationClass = annotationClass;
      this.handler = handler;
      if (rule != null) {
        this.rules = new HashSet<RuleDef>(rule);
      }
    }

    @Override
    public int compareTo(final ProcessingEntry processingEntry) {
      if (rules != null) {
        for (final RuleDef def : rules) {
          if (!def.getRelAnnotation().equals(annotationClass)) {
            continue;
          }

          switch (def.getOrder()) {
            case After:
              return 1;
            case Before:
              return -1;
          }
        }
      }
      else if (processingEntry.rules != null) {
        //noinspection unchecked
        for (final RuleDef def : processingEntry.rules) {
          if (!def.getRelAnnotation().equals(annotationClass)) {
            continue;
          }

          switch (def.getOrder()) {
            case After:
              return -1;
            case Before:
              return 1;
          }
        }
      }

      return -1;
    }

    @Override
    public String toString() {
      return "Scope:" + annotationClass.getName();
    }
  }

  private static interface ProcessingDelegate {
    public boolean process();

    public void processDependencies();
  }
}
