package org.jboss.errai.aerogear.api.pipeline;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.net.URL;
import java.util.List;

/**
 * A {@link Pipe} represents a server connection. An object of this class is responsible to communicate
 * with the server in order to perform read/write operations.
 *
 * @param <T> The data type of the {@link Pipe} operation
 */
public interface Pipe<T> {

  /**
   * Returns the connection type of this {@link Pipe} object (e.g. <code>REST</code>).
   *
   * @return the connection type
   */
  PipeType getType();

  /**
   * Sends a signal to the Pipe to read its data and return it via the callback.
   *
   * @param callback The callback for consuming the result from the {@link Pipe} invocation.
   */
  void read(AsyncCallback<List<T>> callback);

  /**
   * Saves or updates a given object on the server.
   *
   * @param item     the item to save or update
   * @param callback The callback for consuming the result from the {@link Pipe} invocation.
   */
  void save(T item, AsyncCallback<T> callback);

  /**
   * Removes an object from the underlying server connection. The given key argument is used as the objects ID.
   *
   * @param id       representing the ‘id’ of the object to be removed
   * @param callback The callback for consuming the result from the {@link Pipe} invocation.
   */
  void remove(String id, AsyncCallback<Void> callback);
}
