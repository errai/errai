package org.jboss.errai.jpa.client.local.backend;

import org.jboss.errai.jpa.client.local.ErraiEntityManager;

/**
 * Creates instances of StorageBackend, tying each new instance to a particular
 * ErraiEntityManager. This factory class exists to break the reference cycle
 * between WebStorageBackend instances (which want a final reference to an
 * ErraiEntityManager) and ErraiEntityManager (which wants a final reference to
 * its StorageBackend).
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public interface StorageBackendFactory {

  /**
   * Creates a new instance of some implementation of StorageBackend which is
   * permanently bound to the given ErraiEntityManager.
   *
   * @param em
   *          The EntityManager the StorageBackend serves.
   * @return a new instance of some implementation of StorageBackend.
   */
  StorageBackend createInstanceFor(ErraiEntityManager em);

}
