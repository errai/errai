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

import static org.jboss.errai.ioc.rebind.ioc.InjectableInstance.getMethodInjectedInstance;
import static org.jboss.errai.ioc.rebind.ioc.InjectableInstance.getTypeInjectedInstance;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.ioc.rebind.ioc.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.Injector;
import org.jboss.errai.ioc.rebind.ioc.InjectorFactory;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.MetaMethod;

public class IOCProcessorFactory {
  private SortedSet<ProcessingEntry> processingEntries = new TreeSet<ProcessingEntry>();

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

  @SuppressWarnings({"unchecked"})
  public void process(final MetaDataScanner scanner, final IOCProcessingContext context) {
    /**
     * Let's accumulate all the processing tasks.
     */
    for (final ProcessingEntry entry : processingEntries) {
      Class<? extends Annotation> aClass = entry.annotationClass;
      Target target = aClass.getAnnotation(Target.class);

      for (ElementType elementType : target.value()) {
        switch (elementType) {
          case TYPE: {
            Set<Class<?>> classes = scanner.getTypesAnnotatedWith(aClass, context.getPackageFilter());
            for (final Class<?> clazz : classes) {
              if (clazz.getPackage().getName().contains("server")) {
                continue;
              }

              final Annotation aInstance = clazz.getAnnotation(aClass);

              entry.addProcessingDelegate(new ProcessingDelegate<MetaClass>() {
                @Override
                public boolean process() {
                  final MetaClass type = MetaClassFactory.get(clazz);
                  injectorFactory.addType(type);

                  Injector injector = injectorFactory.getInjectionContext().getInjector(type);
                  final InjectableInstance injectableInstance
                          = getTypeInjectedInstance(aInstance, type, injector, injectorFactory.getInjectionContext());
                  return entry.handler.handle(injectableInstance, aInstance, context);
                }

                public String toString() {
                  return clazz.getName();
                }
              });
            }
          }
          break;

          case METHOD: {
            Set<Method> methods = scanner.getMethodsAnnotatedWith(aClass, context.getPackageFilter());

            for (Method method : methods) {
              final Annotation aInstance = method.getAnnotation(aClass);

              final MetaClass type = MetaClassFactory.get(method.getDeclaringClass());
              final MetaMethod metaMethod = MetaClassFactory.get(method);

              entry.addProcessingDelegate(new ProcessingDelegate<MetaField>() {
                @Override
                public boolean process() {
                  injectorFactory.addType(type);
                  Injector injector = injectorFactory.getInjectionContext().getInjector(type);
                  final InjectableInstance injectableInstance
                          = getMethodInjectedInstance(aInstance, metaMethod, injector,
                          injectorFactory.getInjectionContext());
                  return entry.handler.handle(injectableInstance, aInstance, context);
                }

                public String toString() {
                  return type.getFullyQualifiedName();
                }
              });

            }
          }

          case FIELD: {
            Set<Field> fields = scanner.getFieldsAnnotatedWith(aClass, context.getPackageFilter());

            for (Field method : fields) {
              final Annotation aInstance = method.getAnnotation(aClass);

              final MetaClass type = MetaClassFactory.get(method.getDeclaringClass());
              final MetaField metaField = MetaClassFactory.get(method);

              entry.addProcessingDelegate(new ProcessingDelegate<MetaField>() {
                @Override
                public boolean process() {
                  injectorFactory.addType(type);
                  Injector injector = injectorFactory.getInjectionContext().getInjector(type);
                  final InjectableInstance injectableInstance
                          = InjectableInstance.getFieldInjectedInstance(aInstance, metaField, injector,
                          injectorFactory.getInjectionContext());
                  return entry.handler.handle(injectableInstance, aInstance, context);
                }

                public String toString() {
                  return type.getFullyQualifiedName();
                }
              });
            }
          }
        }
      }
    }
  }

  public boolean processAll() {
    int start;

    List<ProcessingEntry> procEntries = new ArrayList<ProcessingEntry>(processingEntries);

    // brute force FTW
    do {
      start = procEntries.size();

      Iterator<ProcessingEntry> iter = procEntries.iterator();

      while (iter.hasNext()) {
        if (iter.next().processAllDelegates()) {
          iter.remove();
        }
      }
    } while (!procEntries.isEmpty() && procEntries.size() < start);


    // aww man, something's screwed.
    if (!procEntries.isEmpty()) {
      // throw a meaningless exception
      throw new RuntimeException("unresolved dependences: " + processingEntries);
    }

    return true;
  }

  private class ProcessingEntry<T> implements Comparable<ProcessingEntry> {
    private Class<? extends Annotation> annotationClass;
    private AnnotationHandler handler;
    private Set<RuleDef> rules;
    private List<ProcessingDelegate<T>> targets = new ArrayList<ProcessingDelegate<T>>();

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

    public boolean processAllDelegates() {
      int start;

      do {
        start = targets.size();

        Iterator<ProcessingDelegate<T>> iterator = targets.iterator();

        while (iterator.hasNext()) {
          if (iterator.next().process()) {
            iterator.remove();
          }
        }

      } while (!targets.isEmpty() && targets.size() < start);

      return targets.isEmpty();
    }

    public void addProcessingDelegate(ProcessingDelegate<T> delegate) {
      targets.add(delegate);
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
      return "Scope:" + annotationClass.getName() + "(" + targets.toString() + "):\n" + targets;
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
  }
}
