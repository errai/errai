package org.jboss.errai.jpa.client.local.backend;

/**
 * Represents a browser-local persistent storage backend.
 * <p>
 * WARNING: this interface is in an extreme state of flux. It is guaranteed to
 * change as the first few alternative implementations are developed.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public interface StorageBackend {

  void put(String key, String value);

  String get(String key);

}
