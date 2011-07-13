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
import java.util.*;

import org.jboss.errai.ioc.rebind.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;

public class InjectionContext {
  private IOCProcessingContext processingContext;
  private Map<MetaClass, List<Injector>> injectors = new LinkedHashMap<MetaClass, List<Injector>>();
  private Map<Class<? extends Annotation>, List<IOCDecoratorExtension>> decorators = new LinkedHashMap<Class<? extends Annotation>, List<IOCDecoratorExtension>>();
  private Map<ElementType, Set<Class<? extends Annotation>>> decoratorsByElementType = new LinkedHashMap<ElementType, Set<Class<? extends Annotation>>>();
  private List<InjectionTask> deferredInjectionTasks = new ArrayList<InjectionTask>();

  private Collection<MetaField> privateFieldsToExpose = new LinkedHashSet<MetaField>();

  public InjectionContext(IOCProcessingContext processingContext) {
    this.processingContext = processingContext;
  }

  public Injector getQualifiedInjector(MetaClass type, QualifyingMetadata metadata) {
    if (metadata == null) {
      metadata = JSR299QualifyingMetadata.createDefaultQualifyingMetaData();
    }

    //todo: figure out why I was doing this.
    MetaClass erased = type;
    List<Injector> injs = injectors.get(erased);
    if (injs != null) {
      for (Injector inj : injs) {
        if (metadata == null && inj.getQualifyingMetadata() == null) {
          return inj;
        }
        else if (metadata != null && inj.getQualifyingMetadata() != null
                && metadata.doesSatisfy(inj.getQualifyingMetadata())) {
          return inj;
        }
      }
    }
    throw new InjectionFailure("could not resolve type for injection: " + erased.getFullyQualifiedName());
  }

  public boolean isInjectable(MetaClass injectorType) {
    if (injectors.containsKey(injectorType)) {
      Injector injector = injectors.get(injectorType).get(0);
      return !(injector.isSingleton() && !injector.isInjected());
    }
    else {
      return false;
    }
  }

  public Injector getInjector(Class<?> injectorType) {
    return getInjector(MetaClassFactory.get(processingContext.loadClassType(injectorType)));
  }

  public Injector getInjector(MetaClass type) {
    MetaClass erased = type;
    if (!injectors.containsKey(erased)) {
      throw new InjectionFailure("could not resolve type for injection: " + erased.getFullyQualifiedName());
    }
    List<Injector> injectorList = injectors.get(erased);
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
    List<Injector> injectorList = injectors.get(injector.getInjectedType());
    if (injectorList == null) {
      injectors.put(injector.getInjectedType(), injectorList = new ArrayList<Injector>());
    }
    else {
      for (Injector inj : injectorList) {
        if (inj.metadataMatches(injector)) {
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

  // Set<Injector> deferred = new HashSet<Injector>();

  public void deferTask(InjectionTask injectionTask) {
//    if (deferred.contains(injectionTask.getInjector().g)) return;
    //   deferred.add(injectionTask.getInjector());

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
      StringBuilder sbuf = new StringBuilder();
      for (InjectionTask task : toExecute) {
        sbuf.append(" - ").append(task.getInjector().getInjectedType()).append("\n");
      }

      throw new RuntimeException("unsatified depedencies:\n" + sbuf);
    }

    //  deferred.clear();
  }

  private Set<String> exposedFields = new HashSet<String>();

  public void addExposedField(MetaField field) {
    if (exposedFields.contains(field.toString())) return;
    exposedFields.add(field.toString());
    privateFieldsToExpose.add(field);
  }

  public Collection<MetaField> getPrivateFieldsToExpose() {
    return Collections.unmodifiableCollection(privateFieldsToExpose);
  }

  public IOCProcessingContext getProcessingContext() {
    return processingContext;
  }
}
