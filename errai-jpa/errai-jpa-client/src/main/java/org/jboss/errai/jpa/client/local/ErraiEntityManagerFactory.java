package org.jboss.errai.jpa.client.local;

/**
 * Implementations of ErraiEntityManagerFactory are responsible for creating
 * instances of ErraiEntityManager. Users of the Errai framework don't normally
 * need to refer to this type directly; instead, they can
 * {@code @Inject EntityManager} directly into application classes, and allow
 * {@link ErraiEntityManagerProvider} to create factories as necessary.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public interface ErraiEntityManagerFactory {

  /**
   * Creates a new ErraiEntityManager configured according to the settings on
   * the implementing factory.
   *
   * @return a new instance of {@link ErraiEntityManager}.
   */
  ErraiEntityManager createEntityManager();
}
