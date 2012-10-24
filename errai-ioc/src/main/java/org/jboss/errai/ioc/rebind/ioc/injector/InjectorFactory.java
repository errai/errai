package org.jboss.errai.ioc.rebind.ioc.injector;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;
import org.jboss.errai.ioc.rebind.ioc.injector.async.AsyncTypeInjector;
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

    addInjector(BootstrapType.Asynchronous, WiringElementType.Type, AsyncTypeInjector.class);
    addInjector(BootstrapType.Asynchronous, WiringElementType.ProducerElement, ProducerInjector.class);
    addInjector(BootstrapType.Asynchronous, WiringElementType.TopLevelProvider, ProviderInjector.class);
  }

  public Injector getInjector(final WiringElementType elementType,
                              final MetaClass type,
                              final InjectionContext context) {
    return getInjector(async ? BootstrapType.Asynchronous : BootstrapType.Synchronous, elementType, type, context);
  }

  public Injector getTypeInjector(final MetaClass type,
                                  final InjectionContext context) {
    return getTypeInjector(async ? BootstrapType.Asynchronous : BootstrapType.Synchronous, type, context);
  }

  public Injector getTypeInjector(final BootstrapType bootstrapType,
                                  final MetaClass type,
                                  final InjectionContext context) {
    return getInjector(bootstrapType, WiringElementType.Type, type, context);
  }

  public Injector getInjector(final BootstrapType bootstrapType,
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
