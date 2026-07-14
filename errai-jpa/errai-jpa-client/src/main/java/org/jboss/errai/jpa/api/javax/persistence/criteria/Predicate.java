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

// $Id: Predicate.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence.criteria;

import java.util.List;

/**
 * The type of a simple or compound predicate: a conjunction or
 * disjunction of restrictions.
 * A simple predicate is considered to be a conjunction with a
 * single conjunct.
 *
 * @since Java Persistence 2.0
 */
public interface Predicate extends Expression<Boolean> {

	public static enum BooleanOperator {
		AND,
		OR
	}

	/**
	 * Return the boolean operator for the predicate.
	 * If the predicate is simple, this is <code>AND</code>.
	 *
	 * @return boolean operator for the predicate
	 */
	BooleanOperator getOperator();

	/**
	 * Whether the predicate has been created from another
	 * predicate by applying the <code>Predicate.not()</code> method
	 * or the <code>CriteriaBuilder.not()</code> method.
	 *
	 * @return boolean indicating if the predicate is
	 *         a negated predicate
	 */
	boolean isNegated();

	/**
	 * Return the top-level conjuncts or disjuncts of the predicate.
	 * Returns empty list if there are no top-level conjuncts or
	 * disjuncts of the predicate.
	 * Modifications to the list do not affect the query.
	 *
	 * @return list of boolean expressions forming the predicate
	 */
	List<Expression<Boolean>> getExpressions();

	/**
	 * Create a negation of the predicate.
	 *
	 * @return negated predicate
	 */
	Predicate not();

}
