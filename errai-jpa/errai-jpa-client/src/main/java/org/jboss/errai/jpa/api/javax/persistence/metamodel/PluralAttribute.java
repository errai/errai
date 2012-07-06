/*
 * Copyright (c) 2008, 2009 Sun Microsystems. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Linda DeMichiel - Java Persistence 2.0 - Version 2.0 (October 1, 2009)
 *     Specification available from http://jcp.org/en/jsr/detail?id=317
 */

// $Id: $

package javax.persistence.metamodel;

/**
 * Instances of the type <code>PluralAttribute</code> represent
 * persistent collection-valued attributes.
 *
 * @param <X> The type the represented collection belongs to
 * @param <C> The type of the represented collection
 * @param <E> The element type of the represented collection
 * @since Java Persistence 2.0
 */
public interface PluralAttribute<X, C, E> extends Attribute<X, C>, Bindable<E> {
	public static enum CollectionType {
		/**
		 * Collection-valued attribute
		 */
		COLLECTION,

		/**
		 * Set-valued attribute
		 */
		SET,

		/**
		 * List-valued attribute
		 */
		LIST,

		/**
		 * Map-valued attribute
		 */
		MAP
	}

	/**
	 * Return the collection type.
	 *
	 * @return collection type
	 */
	CollectionType getCollectionType();

	/**
	 * Return the type representing the element type of the
	 * collection.
	 *
	 * @return element type
	 */
	Type<E> getElementType();
}
