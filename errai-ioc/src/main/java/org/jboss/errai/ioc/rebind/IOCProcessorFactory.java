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

package org.jboss.errai.ioc.rebind;

import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.rebind.EnvironmentUtil;
import org.jboss.errai.ioc.client.api.TestOnly;
import org.jboss.errai.ioc.rebind.ioc.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.InjectionFailure;
import org.jboss.errai.ioc.rebind.ioc.Injector;
import org.jboss.errai.ioc.rebind.ioc.InjectorFactory;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

import static org.jboss.errai.ioc.rebind.ioc.InjectableInstance.getMethodInjectedInstance;
import static org.jboss.errai.ioc.rebind.ioc.InjectableInstance.getTypeInjectedInstance;
import static org.jboss.errai.ioc.rebind.ioc.util.WiringUtil.worstSortAlgorithmEver;

public class IOCProcessorFactory {
  private SortedSet<ProcessingEntry> processingEntries = new TreeSet<ProcessingEntry>();
  private Map<SortUnit, SortUnit> delegates = new LinkedHashMap<SortUnit, SortUnit>();

  private InjectorFactory injectorFactory;

  public IOCProcessorFactory(InjectorFactory factory) {
    this.injectorFactory = factory;
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
        public Set<Class> getClasses() {
          return Collections.singleton(clazz);
        }

        @Override
        public Set<SortUnit> checkDependencies(DependencyControl control, InjectableInstance instance, Annotation annotation, IOCProcessingContext context) {
          return Collections.emptySet();
        }

        @Override
        public boolean handle(InjectableInstance instance, Annotation annotation, IOCProcessingContext context) {
          return false;
        }
      }));
    }
  }


  @SuppressWarnings({"unchecked"})
  public void process(final MetaDataScanner scanner, final IOCProcessingContext context) {
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

    List<SortUnit> list = worstSortAlgorithmEver(delegates.keySet());

    for (SortUnit unit : list) {
      for (Object item : unit.getItems()) {
        if (item instanceof ProcessingDelegate) {
          ((ProcessingDelegate) item).process();
        }
      }
    }
  }


  private void handleType(final ProcessingEntry<?> entry,
                          final DependencyControl dependencyControl,
                          final Class<?> clazz,
                          final Class<? extends Annotation> aClass,
                          final IOCProcessingContext context) {


    final Annotation anno = clazz.getAnnotation(aClass);
    final MetaClass type = MetaClassFactory.get(clazz);

    dependencyControl.masqueradeAs(type);

    if (type.isAnnotationPresent(TestOnly.class) && !EnvironmentUtil.isGWTJUnitTest()) {
      return;
    }

    ProcessingDelegate<MetaClass> del = new ProcessingDelegate<MetaClass>() {
      @Override
      public Set<SortUnit> getRequiredDependencies() {
        final InjectableInstance injectableInstance
                = getTypeInjectedInstance(anno, type, null, injectorFactory.getInjectionContext());

        return entry.handler.checkDependencies(dependencyControl, injectableInstance, anno, context);
      }

      @Override
      public boolean process() {
        injectorFactory.addType(type);

        Injector injector = injectorFactory.getInjectionContext().getInjector(type);
        final InjectableInstance injectableInstance
                = getTypeInjectedInstance(anno, type, injector, injectorFactory.getInjectionContext());

        return entry.handler.handle(injectableInstance, anno, context);
      }

      public MetaClass getType() {
        return type;
      }

      public boolean equals(Object o) {
        return o != null && toString().equals(o.toString());
      }


      public String toString() {
        return clazz.getName();
      }
    };

    Set<SortUnit> requiredDependencies = del.getRequiredDependencies();
    addToDelegates(new SortUnit(((DependencyControlImpl) dependencyControl).masqueradeClass, del, requiredDependencies));
  }

  private void handleMethod(final ProcessingEntry<?> entry,
                            final DependencyControl dependencyControl,
                            final Method method,
                            final Class<? extends Annotation> annoClass,
                            final IOCProcessingContext context) {

    final Annotation anno = method.getAnnotation(annoClass);
    final MetaClass type = MetaClassFactory.get(method.getDeclaringClass());
    final MetaMethod metaMethod = MetaClassFactory.get(method);

    dependencyControl.masqueradeAs(type);

    ProcessingDelegate<MetaField> del = new ProcessingDelegate<MetaField>() {
      @Override
      public Set<SortUnit> getRequiredDependencies() {
        final InjectableInstance injectableInstance
                = getMethodInjectedInstance(anno, metaMethod, null,
                injectorFactory.getInjectionContext());

        return entry.handler.checkDependencies(dependencyControl, injectableInstance, anno, context);
      }

      @Override
      public boolean process() {
        injectorFactory.addType(type);

        Injector injector = injectorFactory.getInjectionContext().getInjector(type);
        final InjectableInstance injectableInstance
                = getMethodInjectedInstance(anno, metaMethod, injector,
                injectorFactory.getInjectionContext());


        return entry.handler.handle(injectableInstance, anno, context);
      }

      public MetaClass getType() {
        return type;
      }

      public boolean equals(Object o) {
        return o != null && toString().equals(o.toString());
      }


      public String toString() {
        return type.getFullyQualifiedName();
      }

    };

    Set<SortUnit> requiredDependencies = del.getRequiredDependencies();
    addToDelegates(new SortUnit(((DependencyControlImpl) dependencyControl).masqueradeClass, del, requiredDependencies));
  }

  private void handleField(final ProcessingEntry<?> entry,
                           final DependencyControl dependencyControl,
                           final Field field,
                           final Class<? extends Annotation> annoClass,
                           final IOCProcessingContext context) {

    final Annotation anno = field.getAnnotation(annoClass);
    final MetaClass type = MetaClassFactory.get(field.getDeclaringClass());
    final MetaField metaField = MetaClassFactory.get(field);

    dependencyControl.masqueradeAs(type);

    ProcessingDelegate<MetaField> del = new ProcessingDelegate<MetaField>() {
      @Override
      public Set<SortUnit> getRequiredDependencies() {
        final InjectableInstance injectableInstance
                = InjectableInstance.getFieldInjectedInstance(anno, metaField, null,
                injectorFactory.getInjectionContext());

        return entry.handler.checkDependencies(dependencyControl, injectableInstance, anno, context);
      }

      @Override
      public boolean process() {
        injectorFactory.addType(type);

        Injector injector = injectorFactory.getInjectionContext().getInjector(type);
        final InjectableInstance injectableInstance
                = InjectableInstance.getFieldInjectedInstance(anno, metaField, injector,
                injectorFactory.getInjectionContext());

        return entry.handler.handle(injectableInstance, anno, context);
      }

      public MetaClass getType() {
        return type;
      }

      public boolean equals(Object o) {
        return o != null && toString().equals(o.toString());
      }


      public String toString() {
        return type.getFullyQualifiedName();
      }
    };

    Set<SortUnit> requiredDependencies = del.getRequiredDependencies();
    addToDelegates(new SortUnit(((DependencyControlImpl) dependencyControl).masqueradeClass, del, requiredDependencies));

  }

  private class ProcessingEntry<T> implements Comparable<ProcessingEntry> {
    private Class<? extends Annotation> annotationClass;
    private AnnotationHandler handler;
    private Set<RuleDef> rules;
    private Set<InjectionFailure> errors = new LinkedHashSet<InjectionFailure>();

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

    public Collection<InjectionFailure> getErrorList() {
      return Collections.unmodifiableCollection(errors);
    }

    @Override
    public int compareTo(ProcessingEntry processingEntry) {
      if (rules != null) {
        for (RuleDef def : rules) {
          if (!def.relAnnotation.equals(annotationClass)) {
            continue;
          }

          switch (def.order) {
            case After:
              return 1;
            case Before:
              return -1;
          }
        }
      }
      else if (processingEntry.rules != null) {
        for (RuleDef def : (Set<RuleDef>) processingEntry.rules) {
          if (!def.relAnnotation.equals(annotationClass)) {
            continue;
          }

          switch (def.order) {
            case After:
              return -1;
            case Before:
              return 1;
          }
        }
      }

      return -1;
    }


    public String toString() {
      return "Scope:" + annotationClass.getName();
    }
  }

  static class RuleDef {
    private Class<? extends Annotation> relAnnotation;
    private RelativeOrder order;

    RuleDef(Class<? extends Annotation> relAnnotation, RelativeOrder order) {
      this.relAnnotation = relAnnotation;
      this.order = order;
    }
  }

  private static interface ProcessingDelegate<T> {
    public boolean process();

    public Set<SortUnit> getRequiredDependencies();
  }
}
