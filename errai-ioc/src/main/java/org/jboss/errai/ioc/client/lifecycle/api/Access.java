package org.jboss.errai.ioc.client.lifecycle.api;

public interface Access<T> extends LifecycleEvent<T> {
  
  public boolean isMethodAccess();
  
  public void setIsMethodAccess(boolean isMethodAccessed);
  
  public void setMethodOrFieldName(String methodOrFieldName);
  
  public String getMethodOrFieldName();

}
