package org.jboss.errai.ioc.client.lifecycle.api;

/**
 * An event for when an IOC bean instance has a method or field accessed.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface Access<T> extends LifecycleEvent<T> {
  
  /**
   * @return True if a method has been accessed. False if a field has been accessed.
   */
  public boolean isMethodAccess();
  
  /**
   * This must be set before the event is fired.
   * 
   * @param isMethodAccessed True if a method has been accessed. False if a field has been accessed.
   */
  public void setIsMethodAccess(boolean isMethodAccessed);
  
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
