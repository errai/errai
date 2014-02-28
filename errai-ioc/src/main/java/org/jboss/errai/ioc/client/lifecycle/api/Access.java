package org.jboss.errai.ioc.client.lifecycle.api;

/**
 * An event for when an IOC bean instance has a method or field accessed.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface Access<T> extends LifecycleEvent<T> {
  
  /**
   * @return True if a method has been accessed.
   */
  public boolean isMethodAccess();
  
  /**
   * @return True if a field has been accessed.
   */
  public boolean isFieldAccess();
  
  /**
   * This must be set before the event is fired. Defaults to false if not set.
   * 
   * @param isMethodAccessed True if a method has been accessed.
   */
  public void setIsMethodAccess(boolean isMethodAccessed);
  
  /**
   * This must be set before the event is fired. Defaults to false if not set.
   * 
   * @param isFieldAccessed True if a field has been accessed.
   */
  public void setIsFieldAccess(boolean isFieldAccessed);
  
  /**
   * This must be set before the event is fired.
   * 
   * @param methodOrFieldName The name of the field or method that was accessed.
   */
  public void setMethodOrFieldName(String methodOrFieldName);
  
/**
 * @return The name of the field or method that was accessed.
 */
  public String getMethodOrFieldName();

}
