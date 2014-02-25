package org.jboss.errai.ioc.client.lifecycle.api;

public interface AuditableLifecycleListener<T> extends LifecycleListener<T> {
  
  public LifecycleListenerGenerator<T> getGenerator();
  
  public LifecycleListener<T> unwrap();

}
