package org.jboss.errai.jpa.client.local;

/**
 * Common interface for ID generators in Errai JPA.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 *
 * @param <T> the type of the ID being generated.
 */
public interface ErraiIdGenerator<T> {

  boolean hasNext(ErraiEntityManager em);

  T next(ErraiEntityManager em);
}
