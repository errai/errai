package org.jboss.errai.bus.client.api;

/**
 * @author Mike Brock
 */
public interface Caller<T> {
  public T call(RemoteCallback<?> callback);
  
  public T call(ErrorCallback errorCallback);
  
  public T call(RemoteCallback<?> callback, ErrorCallback errorCallback);
}
