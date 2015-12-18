package org.jboss.errai.bus.client.api;

/**
 * A <tt>CallableFuture</tt> is something that can be used to reply to an RPC call asynchronously. Particularly for
 * long running processes. RPC methods which return this type will be treated automatically as asynchronous.
 * <p>
 * The RPC reply only occurs when the {@link #setValue(Object)} method is invoked.
 *
 * @author Mike Brock
 */
public interface CallableFuture<T> {

  /**
   * The method called to provide the result value into the future. Calling this method immediately dispatches
   * a response to the caller, completing the asynchronous call.
   *
   * @param responseValue
   *    the response value of the call.
   */
  public void setValue(T responseValue);
}
