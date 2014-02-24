package org.jboss.errai.ioc.client.lifecycle.api;

import java.util.Set;

/**
 * An event representing a change in a bean instances internal state.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface StateChange<T> extends LifecycleEvent<T> {
  
  /**
   * @return The set of names of fields whose values changed.
   */
  public Set<String> getChangedFieldNames();
  
  /**
   * This must be set before the event is fired.
   * 
   * @param changedFieldNames The set of names of fields whose values changed.
   */
  public void setChangedFieldNames(Set<String> changedFieldNames);
  
}
