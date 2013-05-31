package org.jboss.errai.bus.client.api;

import org.jboss.errai.bus.server.api.ServerCallableFuture;

/**
 * Used for obtaining instances of {@link CallableFuture} for use in asynchronous RPC methods.
 *
 * @author Mike Brock
 */
public class CallableFutureFactory {
  private static final CallableFutureFactory CALLABLE_FUTURE_FACTORY
      = new CallableFutureFactory();

  private CallableFutureFactory() {
  }

  public static CallableFutureFactory get() {
    return CALLABLE_FUTURE_FACTORY;
  }

  /**
   * Creates a new {@code CallableFuture} that can be returned for an asynchronous RPC method and used
   * for providing a value back to the client when a long-running process is done.
   *
   * @param returnType The type of value to be returned.
   * @param <T> The type of value to be returned.
   * @return
   *          and instance of the {@link CallableFuture}.
   */
  public <T> CallableFuture<T> createFuture(final Class<T> returnType) {
    return new ServerCallableFuture<T>();
  }
}
