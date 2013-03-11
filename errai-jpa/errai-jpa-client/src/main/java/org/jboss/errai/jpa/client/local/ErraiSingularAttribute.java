package org.jboss.errai.jpa.client.local;

import javax.persistence.metamodel.SingularAttribute;

/**
 * Extends the JPA SingularAttribute interface with methods required by Errai
 * persistence but missing from the JPA metamodel.
 *
 * @param <X>
 *          The type containing the represented attribute
 * @param <T>
 *          The type of the represented attribute
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public interface ErraiSingularAttribute<X, T> extends ErraiAttribute<X, T>, SingularAttribute<X, T> {

  /**
   * Can the attribute's value be generated (usually for ID attributes).
   */
  public boolean isGeneratedValue();

  /**
   * Returns a generator for the values of this attribute. Only works for
   * attributes that are annotated with {@code @GeneratedValue}.
   *
   * @return the ID generator for this generated attribute. Never null.
   * @throws UnsupportedOperationException
   *           if this attribute is not a {@code @GeneratedValue}.
   */
  public ErraiIdGenerator<T> getValueGenerator();
}
