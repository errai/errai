package org.jboss.errai.ui.rebind.ioc.element;

import org.jboss.errai.ioc.rebind.ioc.bootstrapper.AbstractBodyGenerator;
import org.jboss.errai.ioc.rebind.ioc.graph.api.CustomFactoryInjectable;
import org.jboss.errai.ioc.rebind.ioc.graph.api.InjectionSite;
import org.jboss.errai.ioc.rebind.ioc.graph.impl.DefaultCustomFactoryInjectable;
import org.jboss.errai.ioc.rebind.ioc.graph.impl.FactoryNameGenerator;
import org.jboss.errai.ioc.rebind.ioc.graph.impl.InjectableHandle;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableProvider;

import javax.enterprise.context.Dependent;

import static java.util.Collections.singletonList;
import static org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder.InjectableType.ExtensionProvided;
import static org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType.DependentBean;

class ElementProvider implements InjectableProvider {

  private final InjectableHandle handle;
  private final AbstractBodyGenerator injectionBodyGenerator;

  private CustomFactoryInjectable injectable;

  ElementProvider(final InjectableHandle handle, final AbstractBodyGenerator injectionBodyGenerator) {
    this.handle = handle;
    this.injectionBodyGenerator = injectionBodyGenerator;
  }

  @Override
  public CustomFactoryInjectable getInjectable(final InjectionSite injectionSite,
          final FactoryNameGenerator nameGenerator) {

    if (injectable == null) {
      injectable = buildCustomFactoryInjectable(nameGenerator);
    }

    return injectable;
  }

  private CustomFactoryInjectable buildCustomFactoryInjectable(final FactoryNameGenerator nameGenerator) {
    final String factoryName = nameGenerator.generateFor(handle.getType(), handle.getQualifier(), ExtensionProvided);

    return new DefaultCustomFactoryInjectable(handle.getType(), handle.getQualifier(), factoryName, Dependent.class,
            singletonList(DependentBean), injectionBodyGenerator);
  }

}
