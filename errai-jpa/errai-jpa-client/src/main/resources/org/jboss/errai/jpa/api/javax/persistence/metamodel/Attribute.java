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

// $Id: Attribute.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence.metamodel;

/**
 * Represents an attribute of a Java type.
 *
 * @param <X> The represented type that contains the attribute
 * @param <Y> The type of the represented attribute
 * @since Java Persistence 2.0
 */
public interface Attribute<X, Y> {
	public static enum PersistentAttributeType {
		/**
		 * Many-to-one association
		 */
		MANY_TO_ONE,

		/**
		 * One-to-one association
		 */
		ONE_TO_ONE,

		/**
		 * Basic attribute
		 */
		BASIC,

		/**
		 * Embeddable class attribute
		 */
		EMBEDDED,

		/**
		 * Many-to-many association
		 */
		MANY_TO_MANY,

		/**
		 * One-to-many association
		 */
		ONE_TO_MANY,

		/**
		 * Element collection
		 */
		ELEMENT_COLLECTION
	}

	/**
	 * Return the name of the attribute.
	 *
	 * @return name
	 */
	String getName();

	/**
	 * Return the persistent attribute type for the attribute.
	 *
	 * @return persistent attribute type
	 */
	PersistentAttributeType getPersistentAttributeType();

	/**
	 * Return the managed type representing the type in which
	 * the attribute was declared.
	 *
	 * @return declaring type
	 */
	ManagedType<X> getDeclaringType();

	/**
	 * Return the Java type of the represented attribute.
	 *
	 * @return Java type
	 */
	Class<Y> getJavaType();

//	/**
//	 * Return the <code>java.lang.reflect.Member</code> for the represented
//	 * attribute.
//	 *
//	 * @return corresponding <code>java.lang.reflect.Member</code>
//	 */
//	java.lang.reflect.Member getJavaMember();

	/**
	 * Is the attribute an association.
	 *
	 * @return boolean indicating whether the attribute
	 *         corresponds to an association
	 */
	boolean isAssociation();

	/**
	 * Is the attribute collection-valued (represents a Collection,
	 * Set, List, or Map).
	 *
	 * @return boolean indicating whether the attribute is
	 *         collection-valued
	 */
	boolean isCollection();
}
