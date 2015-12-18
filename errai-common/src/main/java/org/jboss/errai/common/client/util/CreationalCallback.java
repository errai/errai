package org.jboss.errai.common.client.util;

/**
 * @author Mike Brock
 */
public interface CreationalCallback<T> {
  public void callback(T beanInstance);
}
