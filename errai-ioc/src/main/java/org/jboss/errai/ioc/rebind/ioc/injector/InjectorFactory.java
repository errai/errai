package org.jboss.errai.ioc.rebind.ioc.injector;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;
import org.jboss.errai.ioc.rebind.ioc.injector.async.AsyncContextualProviderInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.async.AsyncProviderInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.async.AsyncTypeInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.basic.ContextualProviderInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.basic.ProducerInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.basic.ProviderInjector;
import org.jboss.errai.ioc.rebind.ioc.injector.basic.TypeInjector;

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

    addInjector(BootstrapType.Asynchronous, WiringElementType.Type, AsyncTypeInjector.class);
    addInjector(BootstrapType.Asynchronous, WiringElementType.ProducerElement, ProducerInjector.class);
    addInjector(BootstrapType.Asynchronous, WiringElementType.TopLevelProvider, AsyncProviderInjector.class);
    addInjector(BootstrapType.Asynchronous, WiringElementType.ContextualTopLevelProvider,
        AsyncContextualProviderInjector.class);
  }

  private BootstrapType getDefaultBootstrapType() {
    return async ? BootstrapType.Asynchronous : BootstrapType.Synchronous;
  }

  public Injector getInjector(final WiringElementType elementType,
                              final MetaClass type,
                              final InjectionContext context) {
    return getTypeInjector(getDefaultBootstrapType(), elementType, type, context);
  }

  public Injector getTypeInjector(final MetaClass type,
                                  final InjectionContext context) {
    return getTypeInjector(async ? BootstrapType.Asynchronous : BootstrapType.Synchronous, type, context);
  }

  public Injector getTypeInjector(final BootstrapType bootstrapType,
                                  final MetaClass type,
                                  final InjectionContext context) {
    return getTypeInjector(bootstrapType, WiringElementType.Type, type, context);
  }

  private Injector getTypeInjector(final BootstrapType bootstrapType,
                                   final WiringElementType elementType,
                                   final MetaClass type,
                                   final InjectionContext context) {
    final Class<? extends Injector> injectorClass = injectors.get(bootstrapType).get(elementType);

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
    return getProviderInjector(async ? BootstrapType.Asynchronous : BootstrapType.Synchronous, WiringElementType.TopLevelProvider, type, providerType, context);
  }

  public Injector getProviderInjector(final BootstrapType bootstrapType,
                                      final WiringElementType elementType,
                                      final MetaClass type,
                                      final MetaClass providerType,
                                      final InjectionContext context) {
    final Class<? extends Injector> injectorClass = injectors.get(bootstrapType).get(elementType);

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
    return getProviderInjector(async ? BootstrapType.Asynchronous : BootstrapType.Synchronous,
        WiringElementType.ContextualTopLevelProvider, type, providerType, context);
  }

  public Injector getContextualProviderInjector(final BootstrapType bootstrapType,
                                                final WiringElementType elementType,
                                                final MetaClass type,
                                                final MetaClass providerType,
                                                final InjectionContext context) {
    final Class<? extends Injector> injectorClass = injectors.get(bootstrapType).get(elementType);

    try {
      final Constructor<? extends Injector> constructor
          = injectorClass.getConstructor(MetaClass.class, MetaClass.class, InjectionContext.class);

      return constructor.newInstance(type, providerType, context);
    }
    catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }


  public Injector getProducerInjector(final BootstrapType bootstrapType,
                                      final MetaClass type,
                                      final MetaClass providerType,
                                      final InjectionContext context) {
    return getProducerInjector(bootstrapType, WiringElementType.ProducerElement, type, providerType, context);
  }

  public Injector getProducerInjector(final BootstrapType bootstrapType,
                                      final WiringElementType elementType,
                                      final MetaClass type,
                                      final MetaClass providerType,
                                      final InjectionContext context) {
    final Class<? extends Injector> injectorClass = injectors.get(bootstrapType).get(elementType);

    try {
      final Constructor<? extends Injector> constructor
          = injectorClass.getConstructor(MetaClass.class, MetaClass.class, InjectionContext.class);

      return constructor.newInstance(type, providerType, context);
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
