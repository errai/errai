package org.jboss.errai.ioc.client.lifecycle.api;

public interface LifecycleListenerRegistrar {
  
  public <T> Iterable<LifecycleListener<T>> getListeners(Class<? extends LifecycleEvent<T>> eventType, T instance);
  
  public <T> void registerGenerator(Class<T> lifecycleType, LifecycleListenerGenerator<T> generator);
  
  public <T> void registerListener(T instance, LifecycleListener<T> listener);
  
  public <T> void unregisterGenerator(Class<T> lifecycleType, LifecycleListenerGenerator<T> generator);
  
  public <T> void unregisterListener(T instance, LifecycleListener<T> listener);
  
  public <T> boolean endInstanceLifecycle(T instance);

}
