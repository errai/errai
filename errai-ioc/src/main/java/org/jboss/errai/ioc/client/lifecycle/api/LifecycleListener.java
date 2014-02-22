package org.jboss.errai.ioc.client.lifecycle.api;

public interface LifecycleListener<T> {
  
  public void observeEvent(LifecycleEvent<T> event);
  
  public boolean isObserveableEventType(Class<? extends LifecycleEvent<T>> eventType);

}
