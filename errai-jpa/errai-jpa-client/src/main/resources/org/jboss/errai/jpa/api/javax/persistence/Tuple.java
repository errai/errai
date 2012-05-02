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

package javax.persistence;

import java.util.List;

/**
 * Interface for extracting the elements of a query result tuple.
 *
 * @see TupleElement
 * @since Java Persistence 2.0
 */
public interface Tuple {
	/**
	 * Get the value of the specified tuple element.
	 *
	 * @param tupleElement tuple element
	 *
	 * @return value of tuple element
	 *
	 * @throws IllegalArgumentException if tuple element
	 * does not correspond to an element in the
	 * query result tuple
	 */
	<X> X get(TupleElement<X> tupleElement);

	/**
	 * Get the value of the tuple element to which the
	 * specified alias has been assigned.
	 *
	 * @param alias alias assigned to tuple element
	 * @param type of the tuple element
	 *
	 * @return value of the tuple element
	 *
	 * @throws IllegalArgumentException if alias
	 * does not correspond to an element in the
	 * query result tuple or element cannot be
	 * assigned to the specified type
	 */
	<X> X get(String alias, Class<X> type);

	/**
	 * Get the value of the tuple element to which the
	 * specified alias has been assigned.
	 *
	 * @param alias alias assigned to tuple element
	 *
	 * @return value of the tuple element
	 *
	 * @throws IllegalArgumentException if alias
	 * does not correspond to an element in the
	 * query result tuple
	 */
	Object get(String alias);

	/**
	 * Get the value of the element at the specified
	 * position in the result tuple. The first position is 0.
	 *
	 * @param i position in result tuple
	 * @param type type of the tuple element
	 *
	 * @return value of the tuple element
	 *
	 * @throws IllegalArgumentException if i exceeds
	 * length of result tuple  or element cannot be
	 * assigned to the specified type
	 */
	<X> X get(int i, Class<X> type);

	/**
	 * Get the value of the element at the specified
	 * position in the result tuple. The first position is 0.
	 *
	 * @param i position in result tuple
	 *
	 * @return value of the tuple element
	 *
	 * @throws IllegalArgumentException if i exceeds
	 * length of result tuple
	 */
	Object get(int i);

	/**
	 * Return the values of the result tuple elements as an array.
	 *
	 * @return tuple element values
	 */
	Object[] toArray();

	/**
	 * Return the tuple elements.
	 *
	 * @return tuple elements
	 */
	List<TupleElement<?>> getElements();
}
