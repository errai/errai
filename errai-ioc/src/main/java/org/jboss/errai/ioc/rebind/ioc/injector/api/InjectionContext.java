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

package org.jboss.errai.ioc.rebind.ioc.injector.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaEnum;
import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.FactoryBodyGenerator;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.FactoryGenerator;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessor;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCExtensionConfigurator;
import org.jboss.errai.ioc.rebind.ioc.graph.api.InjectionSite;
import org.jboss.errai.ioc.rebind.ioc.graph.api.QualifierFactory;
import org.jboss.errai.ioc.rebind.ioc.graph.impl.DefaultQualifierFactory;
import org.jboss.errai.ioc.rebind.ioc.graph.impl.FactoryNameGenerator;
import org.jboss.errai.ioc.rebind.ioc.graph.impl.InjectableHandle;
import org.jboss.errai.reflections.util.SimplePackageFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableCollection;

/**
 * At every rebind phase, a single {@link InjectionContext} is used. It contains
 * information on enabled alternatives, whitelisted and blacklisted beans, and
 * annotations associated with various {@link WiringElementType wiring types}.
 *
 * The injection context also stores string-named attributes for sharing data
 * between the {@link IOCProcessor} and separate usages of the
 * {@link FactoryGenerator}.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class InjectionContext {
  private static final Logger log = LoggerFactory.getLogger(InjectionContext.class);

  private final IOCProcessingContext processingContext;

  private final Multimap<WiringElementType, Class<? extends Annotation>> elementBindings = HashMultimap.create();
  private final Multimap<InjectableHandle, InjectableProvider> injectableProviders = HashMultimap.create();
  private final Multimap<InjectableHandle, InjectableProvider> exactTypeInjectableProviders = HashMultimap.create();

  private final Collection<ExtensionTypeCallback> extensionTypeCallbacks = new ArrayList<ExtensionTypeCallback>();

  private final boolean async;

  private final QualifierFactory qualifierFactory;

  private final Set<String> whitelist;
  private final Set<String> blacklist;

  private static final String[] implicitWhitelist = { "org.jboss.errai.*", "com.google.gwt.*" };

  private final Multimap<MetaClass, IOCDecoratorExtension<? extends Annotation>> decorators = HashMultimap.create();
  private final Multimap<ElementType, MetaClass> decoratorsByElementType = HashMultimap.create();
  private final Multimap<MetaClass, Class<? extends Annotation>> metaAnnotationAliases
      = HashMultimap.create();

  private final Map<String, Object> attributeMap = new HashMap<String, Object>();


  private InjectionContext(final Builder builder) {
    this.processingContext = Assert.notNull(builder.processingContext);
    this.qualifierFactory = new DefaultQualifierFactory();
    this.whitelist = Assert.notNull(builder.whitelist);
    this.blacklist = Assert.notNull(builder.blacklist);
    this.async = builder.async;
  }

  public static class Builder {
    private IOCProcessingContext processingContext;
    private boolean async;
    private final HashSet<String> enabledAlternatives = new HashSet<String>();
    private final HashSet<String> whitelist = new HashSet<String>();
    private final HashSet<String> blacklist = new HashSet<String>();

    public static Builder create() {
      return new Builder();
    }

    public Builder processingContext(final IOCProcessingContext processingContext) {
      this.processingContext = processingContext;
      return this;
    }

    public Builder enabledAlternatives(final Collection<String> fqcn) {
      enabledAlternatives.addAll(fqcn);
      return this;
    }

    public Builder addToWhitelist(final Collection<String> items) {
      whitelist.addAll(items);
      return this;
    }

    public Builder addToBlacklist(final Collection<String> items) {
      blacklist.addAll(items);
      return this;
    }

    public Builder asyncBootstrap(final boolean async) {
      this.async = async;
      return this;
    }

    public InjectionContext build() {
      Assert.notNull("the processingContext cannot be null", processingContext);

      return new InjectionContext(this);
    }
  }

  /**
   * Register an {@link InjectableProvider} for injection sites that are
   * sastisfied by the given {@link InjectableHandle}.
   *
   * @param handle
   *          Contains the type and qualifier that the given provider satisfies.
   * @param provider
   *          The
   *          {@link InjectableProvider#getInjectable(InjectionSite, FactoryNameGenerator)}
   *          will be called for every injection site satisified by the given
   *          handle. The returned {@link FactoryBodyGenerator} will be used to
   *          generate factories specific to the given injection sites.
   */
  public void registerInjectableProvider(final InjectableHandle handle, final InjectableProvider provider) {
    injectableProviders.put(handle, provider);
  }

  /**
   * Like
   * {@link #registerInjectableProvider(InjectableHandle, InjectableProvider)},
   * but only injection sites with the exact type of the given
   * {@link InjectableHandle} are satisfied.
   *
   * @param handle
   *          Contains the exact type and qualifier that the given provider
   *          satisfies.
   * @param provider
   *          The
   *          {@link InjectableProvider#getInjectable(InjectionSite, FactoryNameGenerator)}
   *          will be called for every injection site satisfied by the given
   *          handle. The returned {@link FactoryBodyGenerator} will be used to
   *          generate factories specific to the given injection sites.
   */
  public void registerExactTypeInjectableProvider(final InjectableHandle handle, final InjectableProvider provider) {
    exactTypeInjectableProviders.put(handle, provider);
  }

  /**
   * An {@link ExtensionTypeCallback} registered with this method will be called for every type processed the Errai IoC
   * before the dependency graph is built. This gives {@link IOCExtensionConfigurator IOCExtensionConfigurators} a way
   * of registering {@link #registerInjectableProvider(InjectableHandle, InjectableProvider) injectable providers}
   * dynamically.
   *
   * @param callback Never null.
   */
  public void registerExtensionTypeCallback(final ExtensionTypeCallback callback) {
    extensionTypeCallbacks.add(callback);
  }

  public Collection<ExtensionTypeCallback> getExtensionTypeCallbacks() {
    return Collections.unmodifiableCollection(extensionTypeCallbacks);
  }

  public Multimap<InjectableHandle, InjectableProvider> getInjectableProviders() {
    return Multimaps.unmodifiableMultimap(injectableProviders);
  }

  public Multimap<InjectableHandle, InjectableProvider> getExactTypeInjectableProviders() {
    return Multimaps.unmodifiableMultimap(exactTypeInjectableProviders);
  }

  public QualifierFactory getQualifierFactory() {
    return qualifierFactory;
  }

  public boolean isWhitelisted(final MetaClass type) {
    if (whitelist.isEmpty()) {
      return true;
    }

    final SimplePackageFilter implicitFilter = new SimplePackageFilter(Arrays.asList(implicitWhitelist));
    final SimplePackageFilter whitelistFilter = new SimplePackageFilter(whitelist);
    final String fullName = type.getFullyQualifiedName();

    return implicitFilter.apply(fullName) || whitelistFilter.apply(fullName);
  }

  public boolean isBlacklisted(final MetaClass type) {
    final SimplePackageFilter blacklistFilter = new SimplePackageFilter(blacklist);
    final String fullName = type.getFullyQualifiedName();

    return blacklistFilter.apply(fullName);
  }

  public void registerDecorator(final IOCDecoratorExtension<?> iocExtension) {
    final Class<? extends Annotation> annotation = iocExtension.decoratesWith();

    final Target target = annotation.getAnnotation(Target.class);
    if (target != null) {
      final boolean oneTarget = target.value().length == 1;

      for (final ElementType type : target.value()) {
        if (type == ElementType.ANNOTATION_TYPE) {
          // type is a meta-annotation. so we need to map all annotations with this meta-annotation to the decorator extension.

          for (final MetaClass annotationClazz : processingContext.metaClassFinder().findAnnotatedWith(annotation)) {
            if (annotationClazz.isAssignableTo(Annotation.class)) {
              decorators.get(annotationClazz).add(iocExtension);

              if (oneTarget) {
                metaAnnotationAliases.put(annotationClazz, annotation);
              }
            }
          }
          if (oneTarget) {
            return;
          }
        }
      }
    }
    decorators.get(MetaClassFactory.get(annotation)).add(iocExtension);
  }

  private Set<MetaClass> getDecoratorAnnotations() {
    return Collections.unmodifiableSet(decorators.keySet());
  }

  public IOCDecoratorExtension<?>[] getDecorators(final MetaClass annotation) {
    final Collection<IOCDecoratorExtension<?>> decs = decorators.get(annotation);
    final IOCDecoratorExtension<?>[] da = new IOCDecoratorExtension[decs.size()];
    decs.toArray(da);
    return da;
  }

  public Collection<MetaClass> getDecoratorAnnotationsBy(final ElementType type) {
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
    for (final MetaClass a : getDecoratorAnnotations()) {
      if (a.isAnnotationPresent(Target.class)) {
        for (final MetaEnum type : a.getAnnotation(Target.class).get().valueAsArray(MetaEnum[].class)) {
          decoratorsByElementType.get(type.as(ElementType.class)).add(a);
        }
      }
      else {
        for (final ElementType type : ElementType.values()) {
          decoratorsByElementType.get(type).add(a);
        }
      }
    }
  }

  public IOCProcessingContext getProcessingContext() {
    return processingContext;
  }

  public void mapElementType(final WiringElementType type, final Class<? extends Annotation> annotationType) {
    elementBindings.put(type, annotationType);
  }

  public Collection<Class<? extends Annotation>> getAnnotationsForElementType(final WiringElementType type) {
    return unmodifiableCollection(elementBindings.get(type));
  }


  public boolean isElementType(final WiringElementType type, final MetaClass annotationType) {
    return getAnnotationsForElementType(type).stream().anyMatch(annotationType::instanceOf);
  }

  public boolean isElementType(final WiringElementType type, final Class<? extends Annotation> annotation) {
    return getAnnotationsForElementType(type).contains(annotation);
  }

  public Collection<Class<? extends Annotation>> getAllElementBindingRegisteredAnnotations() {
    return elementBindings.values();
  }

  public void setAttribute(final String name, final Object value) {
    attributeMap.put(name, value);
  }

  public Object getAttribute(final String name) {
    return attributeMap.get(name);
  }

  public boolean isAsync() {
    return async;
  }
}
