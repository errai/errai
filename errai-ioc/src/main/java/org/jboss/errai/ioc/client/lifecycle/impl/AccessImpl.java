package org.jboss.errai.ioc.client.lifecycle.impl;

import javax.enterprise.context.Dependent;

import org.jboss.errai.ioc.client.lifecycle.api.Access;

@Dependent
public class AccessImpl<T> extends LifecycleEventImpl<T> implements Access<T> {
  
  private boolean isMethodAccess;
  private String accessedName;

  public AccessImpl(final boolean isMethodAccess, final String accessedName) {
    this.isMethodAccess = isMethodAccess;
    this.accessedName = accessedName;
  }
  
  public AccessImpl() {}

  @Override
  public boolean isMethodAccess() {
    return isMethodAccess;
  }
  
  @Override
  public void setIsMethodAccess(final boolean isMethodAccess) {
    this.isMethodAccess = isMethodAccess;
  }

  @Override
  public String getMethodOrFieldName() {
    return accessedName;
  }
  
  @Override
  public void setMethodOrFieldName(final String methodOrFieldName) {
    this.accessedName = methodOrFieldName;
  }

  @Override
  public Class<?> getEventType() {
    return Access.class;
  }

}
