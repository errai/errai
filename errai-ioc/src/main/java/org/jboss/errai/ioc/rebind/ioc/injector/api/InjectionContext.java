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

package org.jboss.errai.ioc.rebind.ioc.injector.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.jboss.errai.codegen.meta.HasAnnotations;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaField;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.util.GenUtil;
import org.jboss.errai.codegen.util.PrivateAccessType;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCGenerator;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.exception.InjectionFailure;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.Injector;
import org.jboss.errai.ioc.rebind.ioc.injector.ProxyInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.QualifiedTypeInjectorDelegate;
import org.jboss.errai.ioc.rebind.ioc.injector.TypeInjector;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableCollection;

public class InjectionContext {
  private IOCProcessingContext processingContext;

  private Multimap<WiringElementType, Class<? extends Annotation>> elementBindings
          = HashMultimap.create();

  // do not refactor to a MultiMap. the resolution algorithm has dynamic replacement of injectors that is difficult
  // to achieve with a MultiMap
  private Map<MetaClass, List<Injector>> injectors = new LinkedHashMap<MetaClass, List<Injector>>();

  private Multimap<MetaClass, Injector> proxiedInjectors = HashMultimap.create();
  private Multimap<MetaClass, MetaClass> cyclingTypes = HashMultimap.create();

  private Set<String> enabledAlternatives = new HashSet<String>();
  private Set<String> enabledReplacements = new HashSet<String>();

  private Multimap<Class<? extends Annotation>, IOCDecoratorExtension> decorators
          = HashMultimap.create();
  private Multimap<ElementType, Class<? extends Annotation>> decoratorsByElementType
          = HashMultimap.create();

  private Map<MetaField, PrivateAccessType> privateFieldsToExpose = new HashMap<MetaField, PrivateAccessType>();
  private Collection<MetaMethod> privateMethodsToExpose = new LinkedHashSet<MetaMethod>();

  private Map<String, Object> attributeMap = new HashMap<String, Object>();

  private Set<String> exposedMembers = new HashSet<String>();

  public InjectionContext(IOCProcessingContext processingContext) {
    this.processingContext = processingContext;
  }

  public Injector getProxiedInjector(MetaClass type, QualifyingMetadata metadata) {
    //todo: figure out why I was doing this.
    MetaClass erased = type.getErased();
    Collection<Injector> injs = proxiedInjectors.get(erased);
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

  public Injector getQualifiedInjector(MetaClass type, QualifyingMetadata metadata) {
    MetaClass erased = type.getErased();
    List<Injector> injs = injectors.get(erased);
    List<Injector> matching = new ArrayList<Injector>();

    boolean alternativeBeans = false;

    if (injs != null) {
      for (Injector inj : injs) {
        if (inj.matches(type.getParameterizedType(), metadata)) {
          matching.add(inj);
          if (inj.isAlternative()) {
            alternativeBeans = true;
          }
        }
      }
    }

    if (matching.isEmpty()) {
      throw new InjectionFailure(erased);
    }
    else if (matching.size() > 1) {
      if (alternativeBeans) {
        Iterator<Injector> matchIter = matching.iterator();
        while (matchIter.hasNext()) {
          if (!enabledAlternatives.contains(matchIter.next().getInjectedType().getFullyQualifiedName())) {
            matchIter.remove();
          }
        }
      }

      if (IOCGenerator.isTestMode && !enabledReplacements.isEmpty()) {
        for (Injector inj : matching) {
          if (enabledReplacements.contains(inj.getInjectedType().getFullyQualifiedName())) {
            return inj;
          }
        }
      }

      if (matching.isEmpty()) {
        throw new InjectionFailure(erased);
      }
      if (matching.size() == 1) {
        return matching.get(0);
      }

      StringBuilder buf = new StringBuilder();
      for (Injector inj : matching) {
        buf.append("     matching> ").append(inj.toString()).append("\n");
      }

      buf.append("  Note: configure an alternative to take precedence or remove all but one matching bean.");

      throw new InjectionFailure("ambiguous injection type (multiple injectors resolved): "
              + erased.getFullyQualifiedName() + " " + (metadata == null ? "" : metadata.toString()) + ":\n" +
              buf.toString());
    }
    else {
      return matching.get(0);
    }
  }

  public void recordCycle(MetaClass from, MetaClass to) {
    cyclingTypes.put(from, to);
  }

  public boolean cycles(MetaClass from, MetaClass to) {
    return cyclingTypes.containsEntry(from, to);
  }

  public void addProxiedInjector(ProxyInjector proxyInjector) {
    proxiedInjectors.put(proxyInjector.getInjectedType(), proxyInjector);
  }

  public boolean isProxiedInjectorRegistered(MetaClass injectorType, QualifyingMetadata qualifyingMetadata) {
    if (proxiedInjectors.containsKey(injectorType.getErased())) {
      for (Injector inj : proxiedInjectors.get(injectorType.getErased())) {
        if (inj.matches(injectorType.getParameterizedType(), qualifyingMetadata)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isProxiedInjectorAvailable(MetaClass injectorType, QualifyingMetadata qualifyingMetadata) {
    if (proxiedInjectors.containsKey(injectorType.getErased())) {
      for (Injector inj : injectors.get(injectorType.getErased())) {
        if (inj.matches(injectorType.getParameterizedType(), qualifyingMetadata)) {
          return inj.isInjected();
        }
      }
    }
    return false;
  }

  public boolean isInjectorRegistered(MetaClass injectorType, QualifyingMetadata qualifyingMetadata) {
    if (injectors.containsKey(injectorType.getErased())) {
      for (Injector inj : injectors.get(injectorType.getErased())) {
        if (inj.matches(injectorType.getParameterizedType(), qualifyingMetadata)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isInjectableQualified(MetaClass injectorType, QualifyingMetadata qualifyingMetadata) {
    if (injectors.containsKey(injectorType.getErased())) {
      for (Injector inj : injectors.get(injectorType.getErased())) {
        if (inj.matches(injectorType.getParameterizedType(), qualifyingMetadata)) {
          return inj.isInjected();
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
    if (injectorList.isEmpty()) {
      throw new InjectionFailure("could not resolve type for injection: " + erased.getFullyQualifiedName());
    }

    return injectorList.get(0);
  }

  public void registerInjector(Injector injector) {
    _registerInjector(injector.getInjectedType(), injector, true);
  }

  private void _registerInjector(MetaClass type, Injector injector, boolean allowOverride) {
    List<Injector> injectorList = injectors.get(type.getErased());
    if (injectorList == null) {
      injectors.put(type.getErased(), injectorList = new ArrayList<Injector>());

      for (MetaClass iface : type.getInterfaces()) {
        QualifiedTypeInjectorDelegate injectorDelegate
                = new QualifiedTypeInjectorDelegate(iface, injector, iface.getParameterizedType());

        _registerInjector(iface, injectorDelegate, false);
      }
    }
    else if (allowOverride) {
      Iterator<Injector> iter = injectorList.iterator();
      boolean noAdd = false;

      while (iter.hasNext()) {
        Injector inj = iter.next();
        if (type.isAssignableFrom(inj.getInjectedType()) && inj.metadataMatches(injector)) {
          noAdd = true;
        }

        if (inj.isPseudo()) {
          iter.remove();
          noAdd = false;
        }
      }

      if (noAdd) {
        return;
      }

    }

    injectorList.add(injector);
  }

  public void registerDecorator(IOCDecoratorExtension<?> iocExtension) {
    decorators.get(iocExtension.decoratesWith()).add(iocExtension);
  }


  public Set<Class<? extends Annotation>> getDecoratorAnnotations() {
    return Collections.unmodifiableSet(decorators.keySet());
  }

  public IOCDecoratorExtension[] getDecorator(Class<? extends Annotation> annotation) {
    Collection<IOCDecoratorExtension> decs = decorators.get(annotation);
    IOCDecoratorExtension[] da = new IOCDecoratorExtension[decs.size()];
    decs.toArray(da);
    return da;
  }

  public Collection<Class<? extends Annotation>> getDecoratorAnnotationsBy(ElementType type) {
    if (decoratorsByElementType.size() == 0) {
      sortDecorators();
    }
    if (decoratorsByElementType.containsKey(type)) {
      return unmodifiableCollection(decoratorsByElementType.get(type));
    }
    else {
      return Collections.emptySet();
    }
  }

  private void sortDecorators() {
    for (Class<? extends Annotation> a : getDecoratorAnnotations()) {
      if (a.isAnnotationPresent(Target.class)) {
        for (ElementType type : a.getAnnotation(Target.class).value()) {
          decoratorsByElementType.get(type).add(a);
        }
      }
    }
  }

  public void addExposedField(MetaField field, PrivateAccessType accessType) {
    if (!privateFieldsToExpose.containsKey(field)) {
      privateFieldsToExpose.put(field, accessType);
    }
    else if (privateFieldsToExpose.get(field) != accessType) {
      accessType = PrivateAccessType.Both;
    }
    privateFieldsToExpose.put(field, accessType);
  }


  public void addExposedMethod(MetaMethod method) {
    String methodSignature = GenUtil.getPrivateMethodName(method);
    if (!exposedMembers.contains(methodSignature)) {
      exposedMembers.add(methodSignature);
    }
    else {
      return;
    }
    privateMethodsToExpose.add(method);
  }

  public Map<MetaField, PrivateAccessType> getPrivateFieldsToExpose() {
    return Collections.unmodifiableMap(privateFieldsToExpose);
  }

  public Collection<MetaMethod> getPrivateMethodsToExpose() {
    return unmodifiableCollection(privateMethodsToExpose);
  }

  public void addType(MetaClass type) {
    registerInjector(new TypeInjector(type, this));
  }

  public void addPsuedoScopeForType(MetaClass type) {
    TypeInjector inj = new TypeInjector(type, this);
    inj.setReplaceable(true);
    registerInjector(inj);
  }


  public IOCProcessingContext getProcessingContext() {
    return processingContext;
  }

  public void addEnabledAlternative(String name) {
    enabledAlternatives.add(name);
  }

  public void addReplacementType(String name) {
    if (!enabledReplacements.add(name)) {
      throw new RuntimeException("ambiguous replacement type: " + name);
    }
  }

  public void mapElementType(WiringElementType type, Class<? extends Annotation> annotationType) {
    elementBindings.put(type, annotationType);
  }

  public Collection<Class<? extends Annotation>> getAnnotationsForElementType(WiringElementType type) {
    return unmodifiableCollection(elementBindings.get(type));
  }

  public boolean isElementType(WiringElementType type, HasAnnotations hasAnnotations) {
    for (Annotation a : hasAnnotations.getAnnotations()) {
      if (getAnnotationsForElementType(type).contains(a.annotationType())) {
        return true;
      }
    }
    return false;
  }

  public boolean isElementType(WiringElementType type, com.google.gwt.core.ext.typeinfo.HasAnnotations hasAnnotations) {
    for (Annotation a : hasAnnotations.getAnnotations()) {
      if (getAnnotationsForElementType(type).contains(a.annotationType())) {
        return true;
      }
    }
    return false;
  }


  public Collection<Map.Entry<WiringElementType, Class<? extends  Annotation>>> getAllElementMappings() {
    return unmodifiableCollection(elementBindings.entries());
  }


  public void setAttribute(String name, Object value) {
    attributeMap.put(name, value);
  }

  public Object getAttribute(String name) {
    return attributeMap.get(name);
  }

  public boolean hasAttribute(String name) {
    return attributeMap.containsKey(name);
  }
}
