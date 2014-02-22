package org.jboss.errai.ioc.client.lifecycle.api;

public interface LifecycleEvent<T> {
  
  public void fireAsync(LifecycleCallback callback);
  
  public void setInstance(T instance);
  
  public T getInstance();
  
  public void veto();

}
