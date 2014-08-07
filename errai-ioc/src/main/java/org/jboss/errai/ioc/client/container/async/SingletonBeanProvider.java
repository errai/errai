package org.jboss.errai.ioc.client.container.async;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.errai.common.client.util.CreationalCallback;

/**
 * Returns the requested instance or retrieve it if its already available
 * 
 * have concurrency check build in
 * 
 * @author mariusgerwinn
 * 
 * @param <T>
 */
public abstract class SingletonBeanProvider<T> {

  private static Logger log = Logger.getLogger(SingletonBeanProvider.class
          .getName());

  private T instance;

  private boolean inProgressOfBeeingCreated;

  private final List<CreationalCallback<T>> loadingCallbacks = new ArrayList<CreationalCallback<T>>();

  /**
   * in case of singleton this will be set immediatly
   * 
   * in case of lazy singleton this will be null
   */
  public SingletonBeanProvider(T instance) {
    this.instance = instance;
  }

  public void setInstance(T instance) {
    if (inProgressOfBeeingCreated) {
      // skip to prevent loop & not calling postcontruct of that bean
      return;
    }
    this.instance = instance;
    // notifiy callbacks
    for (CreationalCallback<T> c : loadingCallbacks) {
      getInstance(c);
    }
    loadingCallbacks.clear();
  }

  public void getInstance(final CreationalCallback<T> callback) {
    if (instance != null)
      callback.callback(instance);
    else {
      loadingCallbacks.add(callback);
      if (loadingCallbacks.size() <= 1) {
        inProgressOfBeeingCreated = true;
        getNewInstance(new CreationalCallback<T>() {

          @Override
          public void callback(T beanInstance) {
            instance = beanInstance;
            //call onInit
            onNewInstanceCreated(instance, new CreationalCallback<T>() {
              @Override
              public void callback(T beanInstance) {
                for (CreationalCallback<T> c : loadingCallbacks) {
                  getInstance(c);
                }
                loadingCallbacks.clear();

              }
            });
          }
        });
      }
    }
  }

  // will be called right after instance is created so finish (postconstruct)
  // can be invoked
  protected abstract void onNewInstanceCreated(final T newInstance,
          final CreationalCallback<T> callback);

  protected abstract void getNewInstance(CreationalCallback<T> callback);

  public boolean isReady() {
    return instance != null;
  }
}
