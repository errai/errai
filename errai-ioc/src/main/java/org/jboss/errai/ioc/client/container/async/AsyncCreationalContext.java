package org.jboss.errai.ioc.client.container.async;

import org.jboss.errai.ioc.client.container.AbstractCreationalContext;
import org.jboss.errai.ioc.client.container.BeanRef;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.IOCSingletonBean;
import org.jboss.errai.ioc.client.container.InitializationCallback;
import org.jboss.errai.ioc.client.container.ProxyResolver;
import org.jboss.errai.ioc.client.container.Tuple;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class AsyncCreationalContext extends AbstractCreationalContext {
  private final AsyncBeanManager beanManager;

  public AsyncCreationalContext(final AsyncBeanManager beanManager,
                                final Class<? extends Annotation> scope) {
    super(scope);
    this.beanManager = beanManager;
  }

  public AsyncCreationalContext(final AsyncBeanManager beanManager, final boolean immutableContext,
                                final Class<? extends Annotation> scope) {
    super(immutableContext, scope);
    this.beanManager = beanManager;
  }

  @Override
  public void addProxyReference(final Object proxyRef, final Object realRef) {
    beanManager.addProxyReference(proxyRef, realRef);
  }

  public <T> void getInstanceOrNew(final AsyncBeanProvider<T> beanProvider,
                                   final CreationalCallback<T> creationalCallback,
                                   final Class<?> beanType,
                                   final Annotation[] qualifiers) {
    final BeanRef ref = getBeanReference(beanType, qualifiers);

    if (wired.containsKey(ref)) {
      creationalCallback.callback((T) wired.get(ref));
    }
    else {
      beanProvider.getInstance(creationalCallback, this);
    }
  }

  public <T> void getBeanInstance(final CreationalCallback<T> creationalCallback,
                                  final Class<T> beanType,
                                  final Annotation[] qualifiers) {
    final T t = (T) wired.get(getBeanReference(beanType, qualifiers));
    if (t == null) {
      // see if the instance is available in the bean manager
      final Collection<AsyncBeanDef<T>> beanList
          = IOC.getAsyncBeanManager().lookupBeans(beanType, qualifiers);

      if (!beanList.isEmpty()) {
        final AsyncBeanDef<T> bean = beanList.iterator().next();
        if (bean != null && bean instanceof IOCSingletonBean) {
          bean.getInstance(creationalCallback);
          return;
        }
      }
    }
    creationalCallback.callback(t);
  }

  public <T> void getSingletonInstanceOrNew(final AsyncInjectionContext injectionContext,
                                            final AsyncBeanProvider<T> beanProvider,
                                            final CreationalCallback<T> creationalCallback,
                                            final Class<T> beanType,
                                            final Annotation[] qualifiers) {

    getBeanInstance(new CreationalCallback<T>() {
      @Override
      public void callback(final T inst) {
        if (inst != null) {
          creationalCallback.callback(inst);
        }
        else {
          beanProvider.getInstance(new CreationalCallback<T>() {
            @Override
            public void callback(final T beanInstance) {
              injectionContext.addBean(beanType, beanType, beanProvider, true, qualifiers);
              creationalCallback.callback(beanInstance);
            }
          }, AsyncCreationalContext.this);
        }
      }
    }, beanType, qualifiers);

    //  @SuppressWarnings("unchecked") final T inst = (T) getBeanInstance((Class<T>) beanType, qualifiers);


  }

  public void finish(final Runnable finishCallback) {
    resolveAllProxies(new Runnable() {
      @Override
      public void run() {
        fireAllInitCallbacks();
        registerAllBeans();

        finishCallback.run();
      }
    });
  }

  private void resolveAllProxies(final Runnable resolveFinishedCallback) {

    final Iterator<Map.Entry<BeanRef, List<ProxyResolver>>> unresolvedIterator
        = new LinkedHashMap<BeanRef, List<ProxyResolver>>(unresolvedProxies).entrySet().iterator();

    final int initialSize = unresolvedProxies.size();

    while (unresolvedIterator.hasNext()) {
      final Map.Entry<BeanRef, List<ProxyResolver>> entry = unresolvedIterator.next();
      if (wired.containsKey(entry.getKey())) {
        final Object wiredInst = wired.get(entry.getKey());
        for (final ProxyResolver pr : entry.getValue()) {
          pr.resolve(wiredInst);
        }

        final Iterator<Tuple<Object, InitializationCallback>> initCallbacks = initializationCallbacks.iterator();
        while (initCallbacks.hasNext()) {
          final Tuple<Object, InitializationCallback> tuple = initCallbacks.next();
          if (tuple.getKey() == wiredInst) {
            tuple.getValue().init(tuple.getKey());
            initCallbacks.remove();
          }
        }

        unresolvedIterator.remove();
      }
      else {
        final AsyncBeanDef<Object> iocBeanDef =
            IOC.getAsyncBeanManager().lookupBean((Class<Object>) entry.getKey().getClazz(), entry.getKey().getAnnotations());

        if (iocBeanDef != null) {
          if (!wired.containsKey(entry.getKey())) {
            iocBeanDef.getInstance(new CreationalCallback<Object>() {
              @Override
              public void callback(final Object beanInstance) {
                addBean(getBeanReference(entry.getKey().getClazz(), entry.getKey().getAnnotations()), beanInstance);
                resolveAllProxies(resolveFinishedCallback);
              }
            });
          }
          return;
        }
      }
    }

    if (!unresolvedProxies.isEmpty() && initialSize != unresolvedProxies.size()) {
      throw new RuntimeException("unresolved proxy: " + unresolvedProxies.entrySet().iterator().next().getKey());
    }
    else {
      resolveFinishedCallback.run();
    }
  }

  private void registerAllBeans() {
    for (final Object ref : getAllCreatedBeanInstances()) {
      beanManager.addBeanToContext(ref, this);
    }
  }

  public void destroyContext() {

  }
}
