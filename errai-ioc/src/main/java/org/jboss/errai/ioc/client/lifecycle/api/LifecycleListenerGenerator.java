package org.jboss.errai.ioc.client.lifecycle.api;

public interface LifecycleListenerGenerator<T> {
  
  public LifecycleListener<T> newInstance();

}
