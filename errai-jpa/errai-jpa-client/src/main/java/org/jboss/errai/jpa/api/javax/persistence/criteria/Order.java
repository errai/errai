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

// $Id: Order.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence.criteria;

/**
 * An object that defines an ordering over the query results.
 *
 * @since Java Persistence 2.0
 */
public interface Order {
	/**
	 * Switch the ordering.
	 *
	 * @return a new <code>Order</code> instance with the reversed ordering
	 */
	Order reverse();

	/**
	 * Whether ascending ordering is in effect.
	 *
	 * @return boolean indicating whether ordering is ascending
	 */
	boolean isAscending();

	/**
	 * Return the expression that is used for ordering.
	 *
	 * @return expression used for ordering
	 */
	Expression<?> getExpression();
}
