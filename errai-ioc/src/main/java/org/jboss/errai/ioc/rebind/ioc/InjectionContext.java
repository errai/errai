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

package org.jboss.errai.ioc.rebind.ioc;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.codegen.framework.util.GenUtil;
import org.jboss.errai.ioc.rebind.IOCProcessingContext;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.ioc.rebind.ioc.exception.UnsatisfiedDependencies;
import org.jboss.errai.ioc.rebind.ioc.exception.UnsatisfiedDependenciesException;
import org.jboss.errai.ioc.rebind.ioc.exception.UnsatisfiedField;
import org.jboss.errai.ioc.rebind.ioc.exception.UnsatisfiedMethod;

public class InjectionContext {
  private IOCProcessingContext processingContext;
  private Map<MetaClass, List<Injector>> injectors = new LinkedHashMap<MetaClass, List<Injector>>();
  private Map<Class<? extends Annotation>, List<IOCDecoratorExtension>> decorators = new LinkedHashMap<Class<? extends Annotation>, List<IOCDecoratorExtension>>();
  private Map<ElementType, Set<Class<? extends Annotation>>> decoratorsByElementType = new LinkedHashMap<ElementType, Set<Class<? extends Annotation>>>();
  private List<InjectionTask> deferredInjectionTasks = new ArrayList<InjectionTask>();
  protected List<Runnable> deferredTasks = new ArrayList<Runnable>();

  private Collection<MetaField> privateFieldsToExpose = new LinkedHashSet<MetaField>();
  private Collection<MetaMethod> privateMethodsToExpose = new LinkedHashSet<MetaMethod>();

  public InjectionContext(IOCProcessingContext processingContext) {
    this.processingContext = processingContext;
  }

  public Injector getQualifiedInjector(MetaClass type, QualifyingMetadata metadata) {
    if (metadata == null) {
      metadata = processingContext.getQualifyingMetadataFactory().createDefaultMetadata();
    }

    //todo: figure out why I was doing this.
    MetaClass erased = type.getErased();
    List<Injector> injs = injectors.get(erased);
    List<Injector> matching = new ArrayList<Injector>();

    if (injs != null) {
      for (Injector inj : injs) {
        if (inj.matches(type.getParameterizedType(), metadata)) {
          matching.add(inj);
        }
      }
    }

    if (matching.isEmpty()) {
      throw new InjectionFailure(erased);
    }
    else if (matching.size() > 1) {
      throw new InjectionFailure("ambiguous injection type (multiple injectors resolved): " + erased
              .getFullyQualifiedName() + (metadata == null ? "" : metadata.toString()));
    }
    else {
      return matching.get(0);
    }
  }

  public boolean isInjectable(MetaClass injectorType) {
    return isInjectableQualified(injectorType, null);
  }


  public boolean isInjectableQualified(MetaClass injectorType, QualifyingMetadata qualifyingMetadata) {
    if (injectors.containsKey(injectorType.getErased())) {
      for (Injector inj : injectors.get(injectorType.getErased())) {
        if (inj.matches(injectorType.getParameterizedType(), qualifyingMetadata)) {
          return !(inj.isSingleton() && !inj.isInjected());
        }
      }
    }
    return false;
  }

  public Injector getInjector(Class<?> injectorType) {
    return getInjector(MetaClassFactory.get(injectorType));
  }

  public Injector getInjector(MetaClass type) {
    MetaClass erased = type.getErased();
    if (!injectors.containsKey(erased)) {
      throw new InjectionFailure("could not resolve type for injection: " + erased.getFullyQualifiedName());
    }
    List<Injector> injectorList = new ArrayList<Injector>(injectors.get(erased));

    Iterator<Injector> iter = injectorList.iterator();
    Injector inj;

    if (injectorList.size() > 1) {
      while (iter.hasNext()) {
        inj = iter.next();

        if (type.getParameterizedType() != null) {
          if (inj.getQualifyingTypeInformation() != null) {
            if (!type.getParameterizedType().isAssignableFrom(inj.getQualifyingTypeInformation())) {
              iter.remove();
            }
          }
        }
        else if (inj.getQualifyingTypeInformation() == null) {
          iter.remove();
        }
      }
    }

    if (injectorList.size() > 1) {
      throw new InjectionFailure("ambiguous injection type (multiple injectors resolved): "
              + erased.getFullyQualifiedName());
    }
    else if (injectorList.isEmpty()) {
      throw new InjectionFailure("could not resolve type for injection: " + erased.getFullyQualifiedName());
    }

    return injectorList.get(0);
  }

  public List<Injector> getInjectorsByType(Class<? extends Injector> injectorType) {
    List<Injector> injs = new LinkedList<Injector>();
    for (List<Injector> inj : injectors.values()) {
      if (injectorType.isAssignableFrom(inj.getClass())) {
        injs.addAll(inj);
      }
    }
    return injs;
  }

  public void registerInjector(Injector injector) {
    _registerInjector(injector.getInjectedType(), injector);
  }

  private void _registerInjector(MetaClass type, Injector injector) {
    List<Injector> injectorList = injectors.get(type.getErased());
    if (injectorList == null) {
      injectors.put(type.getErased(), injectorList = new ArrayList<Injector>());

      for (MetaClass iface : type.getInterfaces()) {
        _registerInjector(iface, new QualifiedTypeInjectorDelegate(injector, iface.getParameterizedType()));
      }
    }
    else {
      for (Injector inj : injectorList) {
        if (type.isAssignableFrom(inj.getInjectedType()) && inj.metadataMatches(injector)) {
          return;
        }
      }
    }

    injectorList.add(injector);
  }

  public void registerDecorator(IOCDecoratorExtension<?> iocExtension) {
    if (!decorators.containsKey(iocExtension.decoratesWith()))
      decorators.put(iocExtension.decoratesWith(), new ArrayList<IOCDecoratorExtension>());

    decorators.get(iocExtension.decoratesWith()).add(iocExtension);
  }

  public void deregisterInjector(Injector injector) {
    List<Injector> injectorList = injectors.get(injector.getInjectedType());
    if (injectorList != null) {
      injectorList.remove(injector);

      if (injectorList.isEmpty()) {
        injectors.remove(injector.getInjectedType());
      }
    }
  }

  public Set<Class<? extends Annotation>> getDecoratorAnnotations() {
    return Collections.unmodifiableSet(decorators.keySet());
  }

  public IOCDecoratorExtension[] getDecorator(Class<? extends Annotation> annotation) {
    List<IOCDecoratorExtension> decs = decorators.get(annotation);
    IOCDecoratorExtension[] da = new IOCDecoratorExtension[decs.size()];
    decs.toArray(da);
    return da;
  }

  public Set<Class<? extends Annotation>> getDecoratorAnnotationsBy(ElementType type) {
    if (decoratorsByElementType.size() == 0) {
      sortDecorators();
    }
    if (decoratorsByElementType.containsKey(type)) {
      return Collections.unmodifiableSet(decoratorsByElementType.get(type));
    }
    else {
      return Collections.emptySet();
    }
  }


  public boolean hasDecoratorsAssociated(ElementType type, Annotation a) {
    if (decoratorsByElementType.size() == 0) {
      sortDecorators();
    }
    return decoratorsByElementType.containsKey(type) && decoratorsByElementType.get(type).contains(a);
  }

  private void sortDecorators() {
    for (Class<? extends Annotation> a : getDecoratorAnnotations()) {
      if (a.isAnnotationPresent(Target.class)) {
        for (ElementType type : a.getAnnotation(Target.class).value()) {
          if (!decoratorsByElementType.containsKey(type)) {
            decoratorsByElementType.put(type, new HashSet<Class<? extends Annotation>>());
          }
          decoratorsByElementType.get(type).add(a);
        }
      }
    }
  }

  public void deferTask(InjectionTask injectionTask) {
    deferredInjectionTasks.add(injectionTask);
  }

  public void runAllDeferred() {

    int start;
    List<InjectionTask> toExecute = new ArrayList<InjectionTask>(deferredInjectionTasks);

    do {
      start = toExecute.size();

      Iterator<InjectionTask> iter = toExecute.iterator();

      while (iter.hasNext()) {
        if (iter.next().doTask(this)) {
          iter.remove();
        }
      }
    } while (!toExecute.isEmpty() && toExecute.size() < start);

    if (!toExecute.isEmpty()) {
      UnsatisfiedDependencies unsatisfiedDependencies = new UnsatisfiedDependencies();
      for (InjectionTask task : toExecute) {
        switch (task.getInjectType()) {
          case PrivateField:
          case Field:
            unsatisfiedDependencies.addUnsatisfiedDependency(
                    new UnsatisfiedField(task.getField(), task.getInjector().getInjectedType(), task.getField().getType()));
            break;

          case PrivateMethod:
          case Method:
            unsatisfiedDependencies.addUnsatisfiedDependency(
                    new UnsatisfiedMethod(task.getMethod(), task.getInjector().getInjectedType(), task.getMethod().getParameters()[0].getType()));
        }
      }

      throw new UnsatisfiedDependenciesException(unsatisfiedDependencies);
    }

    runAllDeferredTasks();    //  deferred.clear();
  }

  public void deferRunnableTask(Runnable runnable) {
    deferredTasks.add(runnable);
  }

  private void runAllDeferredTasks() {
    for (Runnable runnable : deferredTasks) {
      runnable.run();
    }
  }

  private Set<String> exposedMembers = new HashSet<String>();

  public void addExposedField(MetaField field) {
    String fieldSignature = GenUtil.getPrivateFieldInjectorName(field);
    if (exposedMembers.contains(fieldSignature)) return;
    exposedMembers.add(fieldSignature);
    privateFieldsToExpose.add(field);
  }

  public void addExposedMethod(MetaMethod method) {
    String methodSignature = GenUtil.getPrivateMethodName(method);
    if (exposedMembers.contains(methodSignature)) return;
    exposedMembers.add(methodSignature);
    privateMethodsToExpose.add(method);
  }

  public Collection<MetaField> getPrivateFieldsToExpose() {
    return Collections.unmodifiableCollection(privateFieldsToExpose);
  }

  public Collection<MetaMethod> getPrivateMethodsToExpose() {
    return Collections.unmodifiableCollection(privateMethodsToExpose);
  }

  public IOCProcessingContext getProcessingContext() {
    return processingContext;
  }
}
