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

}
