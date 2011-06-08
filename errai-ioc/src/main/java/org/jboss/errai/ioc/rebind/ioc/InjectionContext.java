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

import org.jboss.errai.bus.rebind.ProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.*;

public class InjectionContext {
  private ProcessingContext processingContext;
  private Map<MetaClass, List<Injector>> injectors = new LinkedHashMap<MetaClass, List<Injector>>();
  private Map<Class<? extends Annotation>, List<IOCDecoratorExtension>> decorators = new LinkedHashMap<Class<? extends Annotation>, List<IOCDecoratorExtension>>();
  private Map<ElementType, Set<Class<? extends Annotation>>> decoratorsByElementType = new LinkedHashMap<ElementType, Set<Class<? extends Annotation>>>();
  private List<MetaField> privateFieldsToExpose = new ArrayList<MetaField>();

  public InjectionContext(ProcessingContext processingContext) {
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
        } else if (metadata != null && inj.getQualifyingMetadata() != null
                && metadata.doesSatisfy(inj.getQualifyingMetadata())) {
          return inj;
        }
      }
    }
    throw new InjectionFailure("could not resolve type for injection: " + erased.getFullyQualifedName());
  }

  public Injector getInjector(Class<?> injectorType) {
    return getInjector(MetaClassFactory.get(processingContext.loadClassType(injectorType)));
  }

  public Injector getInjector(MetaClass type) {
    MetaClass erased = type;
    if (!injectors.containsKey(erased)) {
      throw new InjectionFailure("could not resolve type for injection: " + erased.getFullyQualifedName());
    }
    List<Injector> injectorList = injectors.get(erased);
    if (injectorList.size() > 1) {
      throw new InjectionFailure("ambiguous injection type (multiple injectors resolved): "
              + erased.getFullyQualifedName());
    } else if (injectorList.isEmpty()) {
      throw new InjectionFailure("could not resolve type for injection: " + erased.getFullyQualifedName());
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
    } else {
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
    } else {
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

  public List<MetaField> getPrivateFieldsToExpose() {
    return privateFieldsToExpose;
  }

  public ProcessingContext getProcessingContext() {
    return processingContext;
  }
}
