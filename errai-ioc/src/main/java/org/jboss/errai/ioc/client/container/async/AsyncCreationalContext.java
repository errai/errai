package org.jboss.errai.ioc.client.container.async;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.container.AbstractCreationalContext;
import org.jboss.errai.ioc.client.container.BeanRef;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.InitializationCallback;
import org.jboss.errai.ioc.client.container.ProxyResolver;
import org.jboss.errai.ioc.client.container.Tuple;

/**
 * @author Mike Brock
 */
public class AsyncCreationalContext extends AbstractCreationalContext {
  private final AsyncBeanManager beanManager;
  private final AsyncBeanContext beanContext = new AsyncBeanContext();

  private final Map<BeanRef, List<CreationalCallback>> singletonWaitList
      = new HashMap<BeanRef, List<CreationalCallback>>();


  public AsyncCreationalContext(final AsyncBeanManager beanManager,
                                final Class<? extends Annotation> scope) {
    super(scope);
    this.beanManager = beanManager;
    beanContext.setComment("CreationalContext " + scope.getName());
  }

  public AsyncCreationalContext(final AsyncBeanManager beanManager, final boolean immutableContext,
                                final Class<? extends Annotation> scope) {
    super(immutableContext, scope);
    this.beanManager = beanManager;
    beanContext.setComment("CreationalContext " + scope.getName());
  }

  @Override
  public void addProxyReference(final Object proxyRef, final Object realRef) {
    beanManager.addProxyReference(proxyRef, realRef);
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
        if (bean != null && bean instanceof AsyncSingletonBean) {
          //   addWait(new BeanRef(beanType, qualifiers), creationalCallback);
          bean.getInstance(creationalCallback);
          return;
        }
      }
    }
    creationalCallback.callback(t);
  }


  private boolean isWaitedOn(final BeanRef beanRef) {
    return singletonWaitList.containsKey(beanRef);
  }

  /**
   * Add a {@link CreationalCallback} to the wait queue. Or <tt>null</tt> to indicate that the first dependency
   * on that bean has begun to load it.
   *
   * @param beanRef
   *     the bean reference for the callback.
   * @param callback
   *     the instance of the bean.
   * @param <T>
   *     the type of the bean.
   */
  public <T> void addWait(final BeanRef beanRef, final CreationalCallback<T> callback) {
    List<CreationalCallback> callbackList = singletonWaitList.get(beanRef);
    if (callbackList == null) {
      singletonWaitList.put(beanRef, callbackList = new ArrayList<CreationalCallback>());
    }
    if (callback != null) {
      callbackList.add(callback);
    }
  }

  /**
   * Notify all waiting callbacks for the instance result from the specified bean provider.
   *
   * @param beanRef
   *     the bean reference for the callback.
   * @param instance
   *     the instance of the bean.
   * @param <T>
   *     the type of the bean.
   */
  @SuppressWarnings({"unchecked"})
  public <T> void notifyAllWaiting(final BeanRef beanRef, final T instance) {
    final List<CreationalCallback> callbackList = singletonWaitList.get(beanRef);

    if (callbackList != null) {
      for (final CreationalCallback<T> callback : callbackList) {
        callback.callback(instance);
      }
      singletonWaitList.remove(beanRef);
    }
  }

  /**
   * This method is invoked by generated code (in BootstrapperImpl).
   */
  public <T> void getSingletonInstanceOrNew(final AsyncInjectionContext injectionContext,
                                            final AsyncBeanProvider<T> beanProvider,
                                            final CreationalCallback<T> creationalCallback,
                                            final Class<T> beanType,
                                            final Annotation[] qualifiers) {
    getSingletonInstanceOrNew(injectionContext, beanProvider, creationalCallback, beanType, beanType, qualifiers, null);
  }

  /**
   * Implements the singleton loading logic for beans. Because of the lack of ordering guarantees in
   * asynchronous loading, all attempts to load a reference to a singleton should happen via this method within
   * the bean manager. Within the <tt>CreationalContext</tt>, calling this method will insure that only
   * one instance of the specified bean is ever created and returned to the specified {@link CreationalCallback}.
   *
   * @param injectionContext
   *     the current {@link AsyncInjectionContext}
   * @param beanProvider
   *     the reference to the {@link AsyncBeanProvider} which is capable of creating a new instance.
   * @param creationalCallback
   *     the reference to the {@link CreationalCallback} which the instance will be provided to when the bean
   *     has finished loading.
   * @param beanType
   *     the type of the bean.
   * @param qualifiers
   *     the qualifiers for the bean.
   * @param <T>
   *     the parameterized bean type.
   */
  public <T> void getSingletonInstanceOrNew(final AsyncInjectionContext injectionContext,
                                            final AsyncBeanProvider<T> beanProvider,
                                            final CreationalCallback<T> creationalCallback,
                                            final Class type,
                                            final Class<T> beanType,
                                            final Annotation[] qualifiers,
                                            final String name) {

    getBeanInstance(new CreationalCallback<T>() {
      @Override
      public void callback(final T inst) {

        /**
         * If the beanType != type, then this is an aliased reference and we should record it. Otherwise,
         * it must be a lazily initialized singleton reference, and therefore shouldn't be recorded.
         */
        if (!type.equals(beanType)) {
          injectionContext.addBean(type, beanType, beanProvider, inst, qualifiers, name);
        }


        if (inst != null) {
          creationalCallback.callback(inst);
        }
        else {
          final BeanRef beanRef = new BeanRef(beanType, qualifiers);

          // if the bean is already waited on, it means that there is an asynchronous load for the bean
          // already in progress from some other dependent resource or bean.
          if (isWaitedOn(beanRef)) {
            // put the CreationalCallback into the wait queue.
            addWait(beanRef, creationalCallback);
            return;
          }
          else {
            // add a null wait to signify that we have begun the loading process on this bean.
            addWait(beanRef, null);
          }

          final CreationalCallback<T> callback = new CreationalCallback<T>() {
            @Override
            public void callback(final T beanInstance) {
              injectionContext.addBean(type, beanType, beanProvider, beanInstance, qualifiers, name);
              creationalCallback.callback(beanInstance);
              notifyAllWaiting(beanRef, beanInstance);

              // notify we're ready!
              getBeanContext().finish(this);
            }
          };

          // the context cannot finish loading until all outer singletons are loaded.
          getBeanContext().wait(callback);

          // load a new bean!
          beanProvider.getInstance(callback, AsyncCreationalContext.this);
        }
      }
    }, beanType, qualifiers);
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


  public void finish(final Runnable finishCallback) {
    beanContext.runOnFinish(new Runnable() {
      @Override
      public void run() {
        resolveAllProxies(new Runnable() {
          @Override
          public void run() {
            fireAllInitCallbacks();
            registerAllBeans();
            finishCallback.run();
          }
        });
      }
    });
    getBeanContext().finish();
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
                final Iterator<Tuple<Object, InitializationCallback>> initCallbacks = initializationCallbacks.iterator();
                while (initCallbacks.hasNext()) {
                  final Tuple<Object, InitializationCallback> tuple = initCallbacks.next();
                  if (tuple.getKey() == beanInstance) {
                    tuple.getValue().init(tuple.getKey());
                    initCallbacks.remove();
                  }
                }
                resolveAllProxies(resolveFinishedCallback);
              }
            }, this);
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

  public AsyncBeanContext getBeanContext() {
    return beanContext;
  }
}
