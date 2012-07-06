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

// $Id: FetchParent.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence.criteria;

import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 * Represents an element of the from clause which may
 * function as the parent of Fetches.
 *
 * @param <Z>  the source type
 * @param <X>  the target type
 * @since Java Persistence 2.0
 */
public interface FetchParent<Z, X> {
	/**
	 * Return the fetch joins that have been made from this type.
	 * Returns empty set if no fetch joins have been made from
	 * this type.
	 * Modifications to the set do not affect the query.
	 *
	 * @return fetch joins made from this type
	 */
	java.util.Set<Fetch<X, ?>> getFetches();

	/**
	 * Create a fetch join to the specified single-valued attribute
	 * using an inner join.
	 *
	 * @param attribute target of the join
	 *
	 * @return the resulting fetch join
	 */
	<Y> Fetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute);

	/**
	 * Create a fetch join to the specified single-valued attribute
	 * using the given join type.
	 *
	 * @param attribute target of the join
	 * @param jt join type
	 *
	 * @return the resulting fetch join
	 */
	<Y> Fetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute, JoinType jt);

	/**
	 * Create a fetch join to the specified collection-valued
	 * attribute using an inner join.
	 *
	 * @param attribute target of the join
	 *
	 * @return the resulting join
	 */
	<Y> Fetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute);

	/**
	 * Create a fetch join to the specified collection-valued
	 * attribute using the given join type.
	 *
	 * @param attribute target of the join
	 * @param jt join type
	 *
	 * @return the resulting join
	 */
	<Y> Fetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute, JoinType jt);


	//String-based:

	/**
	 * Create a fetch join to the specified attribute using an
	 * inner join.
	 *
	 * @param attributeName name of the attribute for the
	 * target of the join
	 *
	 * @return the resulting fetch join
	 *
	 * @throws IllegalArgumentException if attribute of the given
	 * name does not exist
	 */
	@SuppressWarnings("hiding")
	<X, Y> Fetch<X, Y> fetch(String attributeName);

	/**
	 * Create a fetch join to the specified attribute using
	 * the given join type.
	 *
	 * @param attributeName name of the attribute for the
	 * target of the join
	 * @param jt join type
	 *
	 * @return the resulting fetch join
	 *
	 * @throws IllegalArgumentException if attribute of the given
	 * name does not exist
	 */
	@SuppressWarnings("hiding")
	<X, Y> Fetch<X, Y> fetch(String attributeName, JoinType jt);
}
