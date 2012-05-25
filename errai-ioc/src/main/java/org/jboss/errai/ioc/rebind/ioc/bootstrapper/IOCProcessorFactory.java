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

import com.google.gwt.core.ext.TreeLogger.Type;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaClassMember;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameter;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.util.PrivateAccessType;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.TestMock;
import org.jboss.errai.ioc.client.api.TestOnly;
import org.jboss.errai.ioc.client.container.BeanRef;
import org.jboss.errai.ioc.client.container.CreationalCallback;
import org.jboss.errai.ioc.client.container.CreationalContext;
import org.jboss.errai.ioc.rebind.ioc.extension.AnnotationHandler;
import org.jboss.errai.ioc.rebind.ioc.extension.DependencyControl;
import org.jboss.errai.ioc.rebind.ioc.extension.JSR330AnnotationHandler;
import org.jboss.errai.ioc.rebind.ioc.extension.ProvidedClassAnnotationHandler;
import org.jboss.errai.ioc.rebind.ioc.extension.Rule;
import org.jboss.errai.ioc.rebind.ioc.extension.RuleDef;
import org.jboss.errai.ioc.rebind.ioc.graph.Dependency;
import org.jboss.errai.ioc.rebind.ioc.graph.GraphBuilder;
import org.jboss.errai.ioc.rebind.ioc.graph.SortUnit;
import org.jboss.errai.ioc.rebind.ioc.injector.AbstractInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.ContextualProviderInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.InjectUtil;
import org.jboss.errai.ioc.rebind.ioc.injector.Injector;
import org.jboss.errai.ioc.rebind.ioc.injector.ProducerInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.ProviderInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionPoint;
import org.jboss.errai.ioc.rebind.ioc.injector.api.TaskType;
import org.jboss.errai.ioc.rebind.ioc.injector.api.TypeDiscoveryListener;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;
import org.jboss.errai.ioc.rebind.ioc.metadata.JSR330QualifyingMetadata;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;
import static org.jboss.errai.codegen.util.Stmt.declareVariable;
import static org.jboss.errai.codegen.util.Stmt.loadVariable;
import static org.jboss.errai.ioc.rebind.ioc.graph.GraphSort.sortGraph;
import static org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance.getMethodInjectedInstance;
import static org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance.getInjectedInstance;

@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
public class IOCProcessorFactory {
  private final Stack<SortedSet<ProcessingEntry>> processingTasksStack = new Stack<SortedSet<ProcessingEntry>>();
  private final InjectionContext injectionContext;
  private final Set<String> visitedAutoDiscoveredDependentBeans = new HashSet<String>();

  public IOCProcessorFactory(InjectionContext injectionContext) {
    this.injectionContext = injectionContext;
  }

  public void registerHandler(Class<? extends Annotation> annotation, AnnotationHandler handler) {
    getProcessingTasksSet().add(new ProcessingEntry(annotation, handler));
  }

  public void registerHandler(Class<? extends Annotation> annotation, AnnotationHandler handler, List<RuleDef> rules) {
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

    DependencyControlImpl(Stack<SortedSet<ProcessingEntry>> tasksStack) {
      this.tasksStack = tasksStack;
    }

    @Override
    public void masqueradeAs(MetaClass clazz) {
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
          public void masqueradeAs(MetaClass clazz) {
            // can't masquerade.
          }

          @Override
          public void notifyDependency(MetaClass clazz) {
            if (visitedAutoDiscoveredDependentBeans.contains(clazz.getFullyQualifiedName())) return;
            visitedAutoDiscoveredDependentBeans.add(clazz.getFullyQualifiedName());

            injectionContext.getGraphBuilder().addDependency(dependentClazz, Dependency.on(clazz));
          }

          @Override
          public void notifyDependencies(Collection<MetaClass> clazzes) {
            for (MetaClass clazz : clazzes) {
              notifyDependency(clazz);
            }
          }
        };

        injectionContext.getGraphBuilder().addDependency(masqueradeClass, Dependency.on(dependentClazz));
        JSR330AnnotationHandler.processDependencies(control, dependentClazz, injectionContext);
      }
    }

    @Override
    public void notifyDependencies(Collection<MetaClass> clazzes) {
      for (MetaClass clazz : clazzes) {
        notifyDependency(clazz);
      }
    }
  }

  private void inferHandlers() {
    for (final Map.Entry<WiringElementType, Class<? extends Annotation>> entry : injectionContext.getAllElementMappings()) {
      switch (entry.getKey()) {
        case TopLevelProvider:
          registerHandler(entry.getValue(), new JSR330AnnotationHandler() {
            @Override
            public void getDependencies(DependencyControl control, InjectableInstance instance,
                                        Annotation annotation, IOCProcessingContext context) {

              final MetaClass providerClassType = instance.getType();
              final MetaClass MC_Provider = MetaClassFactory.get(Provider.class);
              final MetaClass MC_ContextualTypeProvider = MetaClassFactory.get(ContextualTypeProvider.class);

              MetaClass providerInterface = null;
              MetaClass providedType;

              if (MC_Provider.isAssignableFrom(providerClassType)) {

                for (MetaClass iface : providerClassType.getInterfaces()) {
                  if (MC_Provider.equals(iface.getErased())) {
                    providerInterface = iface;
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

                MetaType parmType = providerInterface.getParameterizedType().getTypeParameters()[0];
                if (parmType instanceof MetaParameterizedType) {
                  providedType = (MetaClass) ((MetaParameterizedType) parmType).getRawType();
                }
                else {
                  providedType = (MetaClass) parmType;
                }

                injectionContext.registerInjector(new ProviderInjector(providedType, providerClassType, injectionContext));

              }
              else if (MC_ContextualTypeProvider.isAssignableFrom(providerClassType)) {
                for (MetaClass iface : providerClassType.getInterfaces()) {
                  if (MC_ContextualTypeProvider.equals(iface.getErased())) {
                    providerInterface = iface;
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

                MetaType parmType = providerInterface.getParameterizedType().getTypeParameters()[0];
                if (parmType instanceof MetaParameterizedType) {
                  providedType = (MetaClass) ((MetaParameterizedType) parmType).getRawType();
                }
                else {
                  providedType = (MetaClass) parmType;
                }

                injectionContext.registerInjector(new ContextualProviderInjector(providedType, providerClassType, injectionContext));
              }
              else {
                throw new RuntimeException("top level provider " + providerClassType.getFullyQualifiedName()
                        + " does not implement: " + Provider.class.getName() + " or " + ContextualTypeProvider.class);
              }

              control.masqueradeAs(providedType);
              super.getDependencies(control, instance, annotation, context);
            }

            @Override
            public boolean handle(InjectableInstance instance, Annotation annotation, IOCProcessingContext context) {
              return true;
            }
          }, Rule.before(injectionContext.getAnnotationsForElementType(WiringElementType.SingletonBean),
                  injectionContext.getAnnotationsForElementType(WiringElementType.DependentBean)));

          break;
      }
    }

    for (final Map.Entry<WiringElementType, Class<? extends Annotation>> entry : injectionContext.getAllElementMappings()) {
      switch (entry.getKey()) {
        case ProducerElement:
          registerHandler(entry.getValue(), new JSR330AnnotationHandler() {
            @Override
            public void getDependencies(DependencyControl control, final InjectableInstance instance, Annotation annotation,
                                        final IOCProcessingContext context) {

              MetaClass injectedType = instance.getElementTypeOrMethodReturnType();
              MetaClassMember producerMember;

              switch (instance.getTaskType()) {
                case PrivateMethod:
                case Method:
                  producerMember = instance.getMethod();

                  for (MetaParameter parm : instance.getMethod().getParameters()) {
                    control.notifyDependency(injectedType);
                    control.notifyDependencies(fillInInterface(parm.getType().asClass()));
                  }

                  break;
                case PrivateField:
                case Field:
                  producerMember = instance.getField();
                  break;
                default:
                  throw new RuntimeException("illegal producer type");
              }

              ProducerInjector producerInjector
                      = new ProducerInjector(
                      injectionContext,
                      injectedType,
                      producerMember,
                      instance.getQualifyingMetadata(),
                      instance);

              injectionContext.registerInjector(producerInjector);

              control.masqueradeAs(instance.getEnclosingType());

              if (!producerMember.isStatic()) {
                // if this is a static producer, it does not have a dependency on its parent bean
                injectionContext.getGraphBuilder().addDependency(injectedType, Dependency.on(instance.getEnclosingType()));
              }
            }

            @Override
            public boolean handle(final InjectableInstance instance, final Annotation annotation,
                                  final IOCProcessingContext context) {
              return true;
            }

          }, Rule.after(injectionContext.getAnnotationsForElementType(WiringElementType.SingletonBean),
                  injectionContext.getAnnotationsForElementType(WiringElementType.DependentBean)));
          break;

        case DependentBean:
        case SingletonBean:
          registerHandler(entry.getValue(), new JSR330AnnotationHandler() {
            @Override
            public boolean handle(final InjectableInstance type, Annotation annotation, IOCProcessingContext context) {
              injectionContext.getInjector(type.getType()).getBeanInstance(type);
              return true;
            }
          });
          break;
      }
    }
  }

  @SuppressWarnings({"unchecked"})
  public void process(final MetaDataScanner scanner, final IOCProcessingContext context) {
    inferHandlers();
    /**
     * Let's accumulate all the processing tasks.
     */
    do {
      for (final ProcessingEntry entry : processingTasksStack.pop()) {
        Class<? extends Annotation> annoClass = entry.annotationClass;
        Target target = annoClass.getAnnotation(Target.class);

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

        for (ElementType elementType : target.value()) {
          final DependencyControlImpl dependencyControl = new DependencyControlImpl(processingTasksStack);

          switch (elementType) {
            case TYPE: {
              Set<Class<?>> classes;
              if (entry.handler instanceof ProvidedClassAnnotationHandler) {
                classes = ((ProvidedClassAnnotationHandler) entry.handler).getClasses();
              }
              else {
                classes = scanner.getTypesAnnotatedWith(annoClass, context.getPackages());
              }

              for (final Class<?> clazz : classes) {
                handleType(entry, dependencyControl, clazz, annoClass, context);
              }
            }
            break;

            case METHOD: {
              Set<Method> methods = scanner.getMethodsAnnotatedWith(annoClass, context.getPackages());

              for (Method method : methods) {
                handleMethod(entry, dependencyControl, method, annoClass, context);
              }
            }
            break;

            case FIELD: {
              Set<Field> fields = scanner.getFieldsAnnotatedWith(annoClass, context.getPackages());

              for (Field field : fields) {
                handleField(entry, dependencyControl, field, annoClass, context);
              }
            }
          }
        }
      }
    }
    while (!processingTasksStack.isEmpty());

    List<SortUnit> toSort = injectionContext.getGraphBuilder().build();
    List<SortUnit> list = sortGraph(toSort);

    for (SortUnit unit : list) {
      for (Object item : unit.getItems()) {
        if (item instanceof ProcessingDelegate) {
          ((ProcessingDelegate) item).process();
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void handleType(final ProcessingEntry entry,
                          final DependencyControl dependencyControl,
                          final Class<?> clazz,
                          final Class<? extends Annotation> aClass,
                          final IOCProcessingContext context) {


    final Annotation anno = clazz.getAnnotation(aClass);
    final MetaClass type = MetaClassFactory.get(clazz);

    dependencyControl.masqueradeAs(type);

    if (!IOCGenerator.isTestMode) {
      if (type.isAnnotationPresent(TestOnly.class) || type.isAnnotationPresent(TestMock.class)) {
        context.treeLogger.log(Type.DEBUG, "Skipping test-only type " + type.getFullyQualifiedName());
        return;
      }
    }
    final InjectableInstance injectableInstance
            = getInjectedInstance(anno, type, null, injectionContext);

    final ProcessingDelegate del = new ProcessingDelegate() {
      @Override
      public void processDependencies() {
        entry.handler.getDependencies(dependencyControl, injectableInstance, anno, context);
      }

      @Override
      public boolean process() {
        injectionContext.addType(type);

        Injector injector = injectionContext.getInjector(type);
        final InjectableInstance injectableInstance
                = getInjectedInstance(anno, type, injector, injectionContext);

        return entry.handler.handle(injectableInstance, anno, context);
      }

      @Override
      public String toString() {
        return type.getFullyQualifiedName();
      }
    };

    entry.handler.registerMetadata(injectableInstance, anno, context);

    del.processDependencies();

    final MetaClass masq = ((DependencyControlImpl) dependencyControl).masqueradeClass;

    injectionContext.getGraphBuilder().addItem(masq, del);
  }

  @SuppressWarnings("unchecked")
  private void handleMethod(final ProcessingEntry entry,
                            final DependencyControl dependencyControl,
                            final Method method,
                            final Class<? extends Annotation> annoClass,
                            final IOCProcessingContext context) {

    final Annotation anno = method.getAnnotation(annoClass);
    final MetaClass type = MetaClassFactory.get(method.getDeclaringClass());

    final MetaMethod metaMethod = MetaClassFactory.get(method);

    dependencyControl.masqueradeAs(type);

    final InjectableInstance injectableInstance
            = getMethodInjectedInstance(metaMethod, null,
            injectionContext);

    final ProcessingDelegate del = new ProcessingDelegate() {
      @Override
      public void processDependencies() {
        entry.handler.getDependencies(dependencyControl, injectableInstance, anno, context);
      }

      @Override
      public boolean process() {
        injectionContext.addType(type);

        Injector injector = injectionContext.getInjector(type);
        final InjectableInstance injectableInstance
                = getMethodInjectedInstance(metaMethod, injector,
                injectionContext);

        return entry.handler.handle(injectableInstance, anno, context);
      }

      @Override
      public String toString() {
        return type.getFullyQualifiedName();
      }
    };

    entry.handler.registerMetadata(injectableInstance, anno, context);

    del.processDependencies();

    final MetaClass masq = ((DependencyControlImpl) dependencyControl).masqueradeClass;

    injectionContext.getGraphBuilder().addItem(masq, del);
  }

  private void handleField(final ProcessingEntry entry,
                           final DependencyControl dependencyControl,
                           final Field field,
                           final Class<? extends Annotation> annoClass,
                           final IOCProcessingContext context) {

    final Annotation anno = field.getAnnotation(annoClass);
    final MetaClass type = MetaClassFactory.get(field.getDeclaringClass());

    final MetaField metaField = MetaClassFactory.get(field);

    dependencyControl.masqueradeAs(type);

    final ProcessingDelegate del = new ProcessingDelegate() {
      @SuppressWarnings("unchecked")
      @Override
      public void processDependencies() {
        final InjectableInstance injectableInstance
                = InjectableInstance.getFieldInjectedInstance(metaField, null,
                injectionContext);

        entry.handler.getDependencies(dependencyControl, injectableInstance, anno, context);
      }

      @SuppressWarnings("unchecked")
      @Override
      public boolean process() {
        injectionContext.addType(type);

        Injector injector = injectionContext.getInjector(type);
        final InjectableInstance injectableInstance
                = InjectableInstance.getFieldInjectedInstance(metaField, injector,
                injectionContext);

        entry.handler.registerMetadata(injectableInstance, anno, context);

        return entry.handler.handle(injectableInstance, anno, context);
      }

      @Override
      public String toString() {
        return type.getFullyQualifiedName();
      }
    };

    del.processDependencies();

    final MetaClass masq = ((DependencyControlImpl) dependencyControl).masqueradeClass;

    injectionContext.getGraphBuilder().addItem(masq, del);
  }

  private class ProcessingEntry implements Comparable<ProcessingEntry> {
    private Class<? extends Annotation> annotationClass;
    private AnnotationHandler handler;
    private Set<RuleDef> rules;

    private ProcessingEntry(Class<? extends Annotation> annotationClass, AnnotationHandler handler) {
      this.annotationClass = annotationClass;
      this.handler = handler;
    }

    private ProcessingEntry(Class<? extends Annotation> annotationClass, AnnotationHandler handler,
                            List<RuleDef> rule) {
      this.annotationClass = annotationClass;
      this.handler = handler;
      this.rules = new HashSet<RuleDef>(rule);
    }

    @Override
    public int compareTo(ProcessingEntry processingEntry) {
      if (rules != null) {
        for (RuleDef def : rules) {
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
        for (RuleDef def : processingEntry.rules) {
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
