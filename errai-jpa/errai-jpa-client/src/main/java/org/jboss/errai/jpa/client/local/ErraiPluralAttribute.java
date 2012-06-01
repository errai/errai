package org.jboss.errai.jpa.client.local;

import javax.persistence.metamodel.PluralAttribute;

/**
 * Extends the JPA PluralAttribute interface with methods required by Errai
 * persistence but missing from the JPA metamodel. Most importantly, this
 * interface provides methods for reading and writing the attribute value.
 *
 * @param <X>
 *          The type containing the represented attribute
 * @param <C>
 *          The collection type of the represented attribute
 * @param <E>
 *          The element type of the represented collection attribute
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public interface ErraiPluralAttribute<X, C, E> extends ErraiAttribute<X, C>, PluralAttribute<X, C, E> {

  /**
   * Creates a new, empty collection of a type that is assignable to this
   * attribute via the {@link #set(Object, Object)} method. Note that the
   * returned type is not necessarily a subtype of java.util.Collection: it
   * could also be a java.util.Map.
   *
   * @return A new collection instance that is type-compatible with this
   *         attribute's collection type.
   * @see #getCollectionType()
   */
  C createEmptyCollection();

}
