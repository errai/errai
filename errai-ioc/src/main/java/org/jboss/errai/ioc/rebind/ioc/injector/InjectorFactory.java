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

package org.jboss.errai.ioc.rebind.ioc.injector;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassMember;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;
import org.jboss.errai.ioc.rebind.ioc.injector.async.AsyncContextualProviderInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.async.AsyncProducerInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.async.AsyncProviderInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.async.AsyncQualifiedTypeInjectorDelegate;
import org.jboss.errai.ioc.rebind.ioc.injector.async.AsyncTypeInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.basic.ContextualProviderInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.basic.ProducerInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.basic.ProviderInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.basic.QualifiedTypeInjectorDelegate;
import org.jboss.errai.ioc.rebind.ioc.injector.basic.TypeInjector;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class InjectorFactory {
  private final Map<BootstrapType, Map<WiringElementType, Class<? extends Injector>>> injectors
      = new HashMap<BootstrapType, Map<WiringElementType, Class<? extends Injector>>>();

  private final boolean async;

  public InjectorFactory(final boolean async) {
    this.async = async;

    addInjector(BootstrapType.Synchronous, WiringElementType.Type, TypeInjector.class);
    addInjector(BootstrapType.Synchronous, WiringElementType.ProducerElement, ProducerInjector.class);
    addInjector(BootstrapType.Synchronous, WiringElementType.TopLevelProvider, ProviderInjector.class);
    addInjector(BootstrapType.Synchronous, WiringElementType.ContextualTopLevelProvider,
        ContextualProviderInjector.class);
    addInjector(BootstrapType.Synchronous, WiringElementType.QualifiyingType, QualifiedTypeInjectorDelegate.class);

    addInjector(BootstrapType.Asynchronous, WiringElementType.Type, AsyncTypeInjector.class);
    addInjector(BootstrapType.Asynchronous, WiringElementType.ProducerElement, AsyncProducerInjector.class);
    addInjector(BootstrapType.Asynchronous, WiringElementType.TopLevelProvider, AsyncProviderInjector.class);
    addInjector(BootstrapType.Asynchronous, WiringElementType.ContextualTopLevelProvider,
        AsyncContextualProviderInjector.class);
    addInjector(BootstrapType.Asynchronous, WiringElementType.QualifiyingType, AsyncQualifiedTypeInjectorDelegate.class);
  }

  private BootstrapType getDefaultBootstrapType() {
    return async ? BootstrapType.Asynchronous : BootstrapType.Synchronous;
  }

  public Injector getTypeInjector(final MetaClass type,
                                  final InjectionContext context) {
    return getTypeInjector(getDefaultBootstrapType(), type, context);
  }

  private Injector getTypeInjector(final BootstrapType bootstrapType,
                                   final MetaClass type,
                                   final InjectionContext context) {
    final Class<? extends Injector> injectorClass = injectors.get(bootstrapType).get(WiringElementType.Type);

    try {
      final Constructor<? extends Injector> constructor
          = injectorClass.getConstructor(MetaClass.class, InjectionContext.class);

      return constructor.newInstance(type, context);
    }
    catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  public Injector getProviderInjector(final MetaClass type,
                                      final MetaClass providerType,
                                      final InjectionContext context) {
    return getProviderInjector(getDefaultBootstrapType(), type, providerType, context);
  }

  public Injector getProviderInjector(final BootstrapType bootstrapType,
                                      final MetaClass type,
                                      final MetaClass providerType,
                                      final InjectionContext context) {
    final Class<? extends Injector> injectorClass = injectors.get(bootstrapType).get(WiringElementType.TopLevelProvider);

    try {
      final Constructor<? extends Injector> constructor
          = injectorClass.getConstructor(MetaClass.class, MetaClass.class, InjectionContext.class);

      return constructor.newInstance(type, providerType, context);
    }
    catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  public Injector getContextualProviderInjector(final MetaClass type,
                                                final MetaClass providerType,
                                                final InjectionContext context) {
    return getContextualProviderInjector(getDefaultBootstrapType(), type, providerType, context);
  }

  public Injector getContextualProviderInjector(final BootstrapType bootstrapType,
                                                final MetaClass type,
                                                final MetaClass providerType,
                                                final InjectionContext context) {
    final Class<? extends Injector> injectorClass = injectors.get(bootstrapType).get(WiringElementType.ContextualTopLevelProvider);

    try {
      final Constructor<? extends Injector> constructor
          = injectorClass.getConstructor(MetaClass.class, MetaClass.class, InjectionContext.class);

      return constructor.newInstance(type, providerType, context);
    }
    catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  public Injector getProducerInjector(final MetaClass type,
                                      final MetaClassMember providerType,
                                      final InjectableInstance injectableInstance) {
    return getProducerInjector(getDefaultBootstrapType(),
        type, providerType, injectableInstance);
  }


  public Injector getProducerInjector(final BootstrapType bootstrapType,
                                      final MetaClass type,
                                      final MetaClassMember providerType,
                                      final InjectableInstance injectableInstance) {
    final Class<? extends Injector> injectorClass = injectors.get(bootstrapType).get(WiringElementType.ProducerElement);

    try {
      final Constructor<? extends Injector> constructor
          = injectorClass.getConstructor(
          MetaClass.class,
          MetaClassMember.class,
          InjectableInstance.class
      );

      return constructor.newInstance(type, providerType, injectableInstance);
    }
    catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  public Injector getQualifyingTypeInjector(final MetaClass type,
                                            final Injector delegate,
                                            final MetaParameterizedType metaParameterizedType) {

    return getQualifyingTypeInjector(getDefaultBootstrapType(), type, delegate, metaParameterizedType);
  }

  public Injector getQualifyingTypeInjector(final BootstrapType bootstrapType,
                                            final MetaClass type,
                                            final Injector delegate,
                                            final MetaParameterizedType metaParameterizedType) {

    final Class<? extends Injector> injectorClass = injectors.get(bootstrapType).get(WiringElementType.QualifiyingType);

    try {
      final Constructor<? extends Injector> constructor
          = injectorClass.getConstructor(MetaClass.class, Injector.class, MetaParameterizedType.class);

      return constructor.newInstance(type, delegate, metaParameterizedType);
    }
    catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }


  private void addInjector(final BootstrapType type,
                           final WiringElementType elementType,
                           final Class<? extends Injector> injectorClass) {

    Map<WiringElementType, Class<? extends Injector>> wiringElementTypeClassMap = injectors.get(type);
    if (wiringElementTypeClassMap == null) {
      wiringElementTypeClassMap = new HashMap<WiringElementType, Class<? extends Injector>>();
      injectors.put(type, wiringElementTypeClassMap);
    }

    wiringElementTypeClassMap.put(elementType, injectorClass);
  }
}
