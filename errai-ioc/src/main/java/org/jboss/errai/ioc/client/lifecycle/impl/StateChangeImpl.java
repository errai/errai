package org.jboss.errai.ioc.client.lifecycle.impl;

import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.Dependent;

import org.jboss.errai.ioc.client.lifecycle.api.StateChange;

@Dependent
public class StateChangeImpl<T> extends LifecycleEventImpl<T> implements StateChange<T> {
  
  private Set<String> changedFieldNames;
  
  public StateChangeImpl(final Set<String> changedFieldNames) {
    this.changedFieldNames = Collections.unmodifiableSet(changedFieldNames);
  }
  
  public StateChangeImpl() {}

  @Override
  public Set<String> getChangedFieldNames() {
    return changedFieldNames;
  }

  @Override
  public Class<?> getEventType() {
    return StateChange.class;
  }

  @Override
  public void setChangedFieldNames(final Set<String> changedFieldNames) {
    this.changedFieldNames = changedFieldNames;
  }

}
