package org.jboss.errai.jpa.client.local;

import javax.persistence.Parameter;
import javax.persistence.TypedQuery;

import org.jboss.errai.common.client.framework.Assert;

import com.google.common.collect.ImmutableBiMap;

/**
 * Factory class for creating TypedQuery instances. Used internally by
 * EntityManager's <i>createXXXQuery</i> methods.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class TypedQueryFactory {
  private final ErraiEntityManager entityManager;
  private final Class<?> actualResultType;
  private final EntityJsonMatcher matcher;
  private final ImmutableBiMap<String, Parameter<?>> parameters;


  public TypedQueryFactory(
          ErraiEntityManager entityManager,
          Class<?> actualResultType,
          EntityJsonMatcher matcher,
          ErraiParameter<?>[] parameters) {
    this.entityManager = Assert.notNull(entityManager);
    this.actualResultType = Assert.notNull(actualResultType);
    this.matcher = Assert.notNull(matcher);

    ImmutableBiMap.Builder<String, Parameter<?>> pb = ImmutableBiMap.builder();
    for (Parameter<?> p : parameters) {
      pb.put(p.getName(), p);
    }
    this.parameters = pb.build();
  }

  /**
   * Creates an instance of the TypedQuery associated with this factory if its
   * result type is assignable to the given type.
   *
   * @param resultType
   *          The expected result type
   * @param <T>
   *          The result type of the queries produced by this factory
   * @return A new instance of TypedQuery, whose result type is assignable to
   *         {@code resultType}.
   * @throws IllegalArgumentException
   *           if the query's result type is not assignable to the given type.
   */
  public <T> TypedQuery<T> createIfCompatible(Class<T> resultType) {
    if (resultType != actualResultType) {
      throw new IllegalArgumentException("Expected return type " + resultType + " is not assignable from actual return type " + actualResultType);
    }
    return new ErraiTypedQuery<T>(entityManager, resultType, matcher, parameters);
  }
}
