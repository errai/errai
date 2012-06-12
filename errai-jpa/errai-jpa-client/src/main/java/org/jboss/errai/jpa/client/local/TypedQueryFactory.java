package org.jboss.errai.jpa.client.local;

import javax.persistence.TypedQuery;

/**
 * Factory class for creating TypedQuery instances. Used internally by
 * EntityManager's <i>createXXXQuery</i> methods.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public interface TypedQueryFactory {

  /**
   * Creates an instance of the TypedQuery associated with this factory if its
   * result type is assignable to the given type.
   *
   * @param resultType
   *          The expected result type
   * @return A new instance of TypedQuery, whose result type is assignable to
   *         {@code resultType}.
   * @throws IllegalArgumentException
   *           if the query's result type is not assignable to the given type.
   */
  abstract <T> TypedQuery<T> createIfCompatible(Class<T> resultType);
}
