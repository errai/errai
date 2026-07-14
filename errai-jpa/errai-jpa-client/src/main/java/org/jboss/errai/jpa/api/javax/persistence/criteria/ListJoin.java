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

// $Id: ListJoin.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence.criteria;

import java.util.List;
import javax.persistence.metamodel.ListAttribute;

/**
 * The <code>ListJoin</code> interface is the type of the result of
 * joining to a collection over an association or element
 * collection that has been specified as a <code>java.util.List</code>.
 *
 * @param <Z> the source type of the join
 * @param <E> the element type of the target List
 * @since Java Persistence 2.0
 */
public interface ListJoin<Z, E>
		extends PluralJoin<Z, List<E>, E> {

	/**
	 * Return the metamodel representation for the list attribute.
	 *
	 * @return metamodel type representing the <code>List</code> that is
	 *         the target of the join
	 */
	ListAttribute<? super Z, E> getModel();

	/**
	 * Create an expression that corresponds to the index of
	 * the object in the referenced association or element
	 * collection.
	 * This method must only be invoked upon an object that
	 * represents an association or element collection for
	 * which an order column has been defined.
	 *
	 * @return expression denoting the index
	 */
	Expression<Integer> index();
}