package org.jboss.errai.ioc.client.lifecycle.api;

import java.util.Set;


public interface StateChange<T> extends LifecycleEvent<T> {
  
  public Set<String> getChangedFieldNames();
  
  public void setChangedFieldNames(Set<String> changedFieldNames);
  
}
