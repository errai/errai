package org.jboss.errai.ioc.client.lifecycle.impl;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.lifecycle.api.Destruction;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleCallback;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleEvent;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleListener;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleListenerRegistrar;

public abstract class LifecycleEventImpl<T> implements LifecycleEvent<T> {

  private T instance;
  private boolean isVetoed = false;

  @Override
  public void fireAsync() {
    fireAsync(null);
  }

  @Override
  public void fireAsync(final LifecycleCallback callback) {
    IOC.getAsyncBeanManager().lookupBean(LifecycleListenerRegistrar.class)
            .getInstance(new CreationalCallback<LifecycleListenerRegistrar>() {

              @Override
              public void callback(LifecycleListenerRegistrar registrar) {
                isVetoed = false;
                final Iterable<LifecycleListener<T>> listeners = registrar
                        .getListeners((Class<? extends LifecycleEvent<T>>) getEventType(), getInstance());
                for (final LifecycleListener<T> listener : listeners) {
                  if (isVetoed)
                    break;
                  listener.observeEvent(LifecycleEventImpl.this);
                }
                final boolean outcome = !isVetoed;
                if (outcome && getEventType().equals(Destruction.class)) {
                  registrar.endInstanceLifecycle(getInstance());
                }
                if (callback != null) {
                  callback.callback(outcome);
                }
              }
            });
  }

  @Override
  public T getInstance() {
    return instance;
  }

  @Override
  public void setInstance(final T instance) {
    this.instance = instance;
  }

  @Override
  public void veto() {
    isVetoed = true;
  }

  public abstract Class<?> getEventType();

}
