package org.jboss.errai.ioc.client.lifecycle.impl;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.lifecycle.api.Access;
import org.jboss.errai.ioc.client.lifecycle.api.Destruction;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleCallback;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleEvent;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleListener;
import org.jboss.errai.ioc.client.lifecycle.api.LifecycleListenerRegistrar;

public abstract class LifecycleEventImpl<T> implements LifecycleEvent<T> {

  private T instance;
  private boolean isVetoed = false;

  @Override
  public void fireAsync(final T instance) {
    fireAsync(instance, null);
  }

  @Override
  public void fireAsync(final T instance, final LifecycleCallback callback) {
    this.instance = instance;
    IOC.getAsyncBeanManager().lookupBean(LifecycleListenerRegistrar.class)
            .getInstance(new CreationalCallback<LifecycleListenerRegistrar>() {

              @Override
              public void callback(LifecycleListenerRegistrar registrar) {
                isVetoed = false;
                final Iterable<LifecycleListener<T>> listeners = registrar
                        .getListeners((Class<? extends LifecycleEvent<T>>) getEventType(), instance);
                for (final LifecycleListener<T> listener : listeners) {
                  if (isVetoed)
                    break;
                  listener.observeEvent(LifecycleEventImpl.this);
                }
                final boolean outcome = !isVetoed;
                if (outcome && getEventType().equals(Destruction.class)) {
                  registrar.endInstanceLifecycle(instance);
                }
                if (callback != null) {
                  callback.callback(outcome);
                }
              }
            });
  }

  @Override
  public void veto() {
    isVetoed = true;
  }

  /**
   * This must return the interface of the event it represents (i.e.
   * {@link Access} rather than {@link AccessImpl}).
   */
  public abstract Class<?> getEventType();

}
