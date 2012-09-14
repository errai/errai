package org.jboss.errai.demo.jpa.client.local;

/**
 * Callback interface for registering operation handlers for various operations on the {@link AlbumTable}.
 *
 * @param <T> The model type represented by the row.
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public interface RowOperationHandler<T> {

  /**
   * Handle the event by doing whatever you want!
   */
  void handle(T modelObject);

}
