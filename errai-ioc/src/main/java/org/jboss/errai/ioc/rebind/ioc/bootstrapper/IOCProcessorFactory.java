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

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.util.PrivateAccessType;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.TestMock;
import org.jboss.errai.ioc.client.api.TestOnly;
import org.jboss.errai.ioc.rebind.ioc.extension.AnnotationHandler;
import org.jboss.errai.ioc.rebind.ioc.extension.DependencyControl;
import org.jboss.errai.ioc.rebind.ioc.extension.JSR330AnnotationHandler;
import org.jboss.errai.ioc.rebind.ioc.extension.ProvidedClassAnnotationHandler;
import org.jboss.errai.ioc.rebind.ioc.extension.Rule;
import org.jboss.errai.ioc.rebind.ioc.extension.RuleDef;
import org.jboss.errai.ioc.rebind.ioc.graph.SortUnit;
import org.jboss.errai.ioc.rebind.ioc.injector.AbstractInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.ContextualProviderInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.Injector;
import org.jboss.errai.ioc.rebind.ioc.injector.ProviderInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionPoint;
import org.jboss.errai.ioc.rebind.ioc.injector.api.TypeDiscoveryListener;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;
import org.jboss.errai.ioc.rebind.ioc.metadata.JSR330QualifyingMetadata;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

import static org.jboss.errai.ioc.rebind.ioc.graph.GraphSort.sortGraph;
import static org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance.getMethodInjectedInstance;
import static org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance.getTypeInjectedInstance;

@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
public class IOCProcessorFactory {
  private SortedSet<ProcessingEntry> processingEntries = new TreeSet<ProcessingEntry>();
  private Map<SortUnit, SortUnit> delegates = new LinkedHashMap<SortUnit, SortUnit>();

  private InjectionContext injectionContext;

  public IOCProcessorFactory(InjectionContext injectionContext) {
    this.injectionContext = injectionContext;
  }

  public void registerHandler(Class<? extends Annotation> annotation, AnnotationHandler handler) {
    processingEntries.add(new ProcessingEntry(annotation, handler));
  }

  public void registerHandler(Class<? extends Annotation> annotation, AnnotationHandler handler, List<RuleDef> rules) {
    processingEntries.add(new ProcessingEntry(annotation, handler, rules));
  }

  private void addToDelegates(SortUnit unit) {
    if (delegates.containsKey(unit)) {
      SortUnit existing = delegates.get(unit);
      for (Object o : unit.getItems()) {
        existing.addItem(o);
      }
    }
    else {
      delegates.put(unit, unit);
    }
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
    public void addType(final Class<? extends Annotation> annotation, final Class clazz) {
      if (tasksStack.isEmpty()) {
        tasksStack.push(new TreeSet<ProcessingEntry>());
      }
      tasksStack.peek().add(new ProcessingEntry(annotation, new ProvidedClassAnnotationHandler() {

        @Override
        public void registerMetadata(InjectableInstance instance, Annotation annotation, IOCProcessingContext context) {
        }

        @Override
        public Set<Class> getClasses() {
          return Collections.singleton(clazz);
        }

        @Override
        public Set<SortUnit> getDependencies(DependencyControl control, InjectableInstance instance, Annotation annotation, IOCProcessingContext context) {
          return Collections.emptySet();
        }

        @Override
        public boolean handle(InjectableInstance instance, Annotation annotation, IOCProcessingContext context) {
          return false;
        }
      }));
    }
  }

  private void inferHandlers() {
    for (final Map.Entry<WiringElementType, Class<? extends Annotation>> entry : injectionContext.getAllElementMappings()) {
      switch (entry.getKey()) {
        case TopLevelProvider:
          registerHandler(entry.getValue(), new JSR330AnnotationHandler() {
            @Override
            public Set<SortUnit> getDependencies(DependencyControl control, InjectableInstance instance,
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
              return super.getDependencies(control, instance, annotation, context);
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
            public Set<SortUnit> getDependencies(DependencyControl control, final InjectableInstance instance, Annotation annotation,
                                                 final IOCProcessingContext context) {

              switch (instance.getTaskType()) {
                case Type:
                  break;
                case PrivateField:
                case PrivateMethod:
                  instance.ensureMemberExposed(PrivateAccessType.Read);
              }

              injectionContext.registerInjector(new AbstractInjector() {
                {
                  super.qualifyingMetadata = JSR330QualifyingMetadata.createFromAnnotations(instance.getQualifiers());
                  this.provider = true;
                  this.enclosingType = instance.getEnclosingType();

                  if (injectionContext.isInjectorRegistered(enclosingType, qualifyingMetadata)) {
                    setRendered(true);
                  }
                  else {
                    context.registerTypeDiscoveryListener(new TypeDiscoveryListener() {
                      @Override
                      public void onDiscovery(IOCProcessingContext context, InjectionPoint injectionPoint) {
                        if (injectionPoint.getEnclosingType().equals(enclosingType)) {
                          setRendered(true);
                        }
                      }
                    });
                  }
                }

                @Override
                public Statement getBeanInstance(InjectableInstance injectableInstance) {
                  return instance.getValueStatement();
                }

                @Override
                public boolean isSingleton() {
                  return false;
                }

                @Override
                public boolean isPseudo() {
                  return false;
                }

                @Override
                public String getVarName() {
                  return null;
                }

                @Override
                public MetaClass getInjectedType() {
                  switch (instance.getTaskType()) {
                    case PrivateMethod:
                    case Method:
                      return instance.getMethod().getReturnType();
                    case PrivateField:
                    case Field:
                      return instance.getField().getType();
                    default:
                      return null;
                  }
                }
              });

              control.masqueradeAs(instance.getElementTypeOrMethodReturnType());
              return Collections.singleton(new SortUnit(instance.getEnclosingType(), true));
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

    Stack<SortedSet<ProcessingEntry>> processingTasksStack = new Stack<SortedSet<ProcessingEntry>>();
    processingTasksStack.push(processingEntries);

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

    List<SortUnit> list = sortGraph(delegates.keySet());

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
            = getTypeInjectedInstance(anno, type, null, injectionContext);

    ProcessingDelegate del = new ProcessingDelegate() {
      @Override
      public Set<SortUnit> getRequiredDependencies() {
        return entry.handler.getDependencies(dependencyControl, injectableInstance, anno, context);
      }

      @Override
      public boolean process() {
        injectionContext.addType(type);

        Injector injector = injectionContext.getInjector(type);
        final InjectableInstance injectableInstance
                = getTypeInjectedInstance(anno, type, injector, injectionContext);

        return entry.handler.handle(injectableInstance, anno, context);
      }

      @Override
      public String toString() {
        return type.getFullyQualifiedName();
      }
    };

    entry.handler.registerMetadata(injectableInstance, anno, context);

    Set<SortUnit> requiredDependencies = del.getRequiredDependencies();
    addToDelegates(new SortUnit(((DependencyControlImpl) dependencyControl).masqueradeClass, del, requiredDependencies));
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
            = getMethodInjectedInstance(anno, metaMethod, null,
            injectionContext);

    ProcessingDelegate del = new ProcessingDelegate() {
      @Override
      public Set<SortUnit> getRequiredDependencies() {
        return entry.handler.getDependencies(dependencyControl, injectableInstance, anno, context);
      }

      @Override
      public boolean process() {
        injectionContext.addType(type);

        Injector injector = injectionContext.getInjector(type);
        final InjectableInstance injectableInstance
                = getMethodInjectedInstance(anno, metaMethod, injector,
                injectionContext);

        return entry.handler.handle(injectableInstance, anno, context);
      }

      @Override
      public String toString() {
        return type.getFullyQualifiedName();
      }
    };

    entry.handler.registerMetadata(injectableInstance, anno, context);

    Set<SortUnit> requiredDependencies = del.getRequiredDependencies();
    addToDelegates(new SortUnit(((DependencyControlImpl) dependencyControl).masqueradeClass, del, requiredDependencies));
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

    ProcessingDelegate del = new ProcessingDelegate() {
      @SuppressWarnings("unchecked")
      @Override
      public Set<SortUnit> getRequiredDependencies() {
        final InjectableInstance injectableInstance
                = InjectableInstance.getFieldInjectedInstance(anno, metaField, null,
                injectionContext);

        return entry.handler.getDependencies(dependencyControl, injectableInstance, anno, context);
      }

      @SuppressWarnings("unchecked")
      @Override
      public boolean process() {
        injectionContext.addType(type);

        Injector injector = injectionContext.getInjector(type);
        final InjectableInstance injectableInstance
                = InjectableInstance.getFieldInjectedInstance(anno, metaField, injector,
                injectionContext);

        entry.handler.registerMetadata(injectableInstance, anno, context);

        return entry.handler.handle(injectableInstance, anno, context);
      }

      @Override
      public String toString() {
        return type.getFullyQualifiedName();
      }

    };

    Set<SortUnit> requiredDependencies = del.getRequiredDependencies();
    addToDelegates(new SortUnit(((DependencyControlImpl) dependencyControl).masqueradeClass, del, requiredDependencies));
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

    public Set<SortUnit> getRequiredDependencies();
  }
}
