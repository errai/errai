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

// $Id: AbstractQuery.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence.criteria;

import java.util.List;
import java.util.Set;

import javax.persistence.metamodel.EntityType;

/**
 * The <code>AbstractQuery</code> interface defines functionality that is common
 * to both top-level queries and subqueries.
 * It is not intended to be used directly in query construction.
 *
 * <p> All queries must have:
 * a set of root entities (which may in turn own joins).
 * <p> All queries may have:
 * a conjunction of restrictions.
 *
 * @param <T>  the type of the result
 * @since Java Persistence 2.0
 */
public interface AbstractQuery<T> {
	/**
	 * Create and add a query root corresponding to the given entity,
	 * forming a cartesian product with any existing roots.
	 *
	 * @param entityClass the entity class
	 *
	 * @return query root corresponding to the given entity
	 */
	<X> Root<X> from(Class<X> entityClass);

	/**
	 * Create and add a query root corresponding to the given entity,
	 * forming a cartesian product with any existing roots.
	 *
	 * @param entity metamodel entity representing the entity
	 * of type X
	 *
	 * @return query root corresponding to the given entity
	 */
	<X> Root<X> from(EntityType<X> entity);

	/**
	 * Modify the query to restrict the query results according
	 * to the specified boolean expression.
	 * Replaces the previously added restriction(s), if any.
	 *
	 * @param restriction a simple or compound boolean expression
	 *
	 * @return the modified query
	 */
	AbstractQuery<T> where(Expression<Boolean> restriction);

	/**
	 * Modify the query to restrict the query results according
	 * to the conjunction of the specified restriction predicates.
	 * Replaces the previously added restriction(s), if any.
	 * If no restrictions are specified, any previously added
	 * restrictions are simply removed.
	 *
	 * @param restrictions zero or more restriction predicates
	 *
	 * @return the modified query
	 */
	AbstractQuery<T> where(Predicate... restrictions);

	/**
	 * Specify the expressions that are used to form groups over
	 * the query results.
	 * Replaces the previous specified grouping expressions, if any.
	 * If no grouping expressions are specified, any previously
	 * added grouping expressions are simply removed.
	 *
	 * @param grouping zero or more grouping expressions
	 *
	 * @return the modified query
	 */
	AbstractQuery<T> groupBy(Expression<?>... grouping);

	/**
	 * Specify the expressions that are used to form groups over
	 * the query results.
	 * Replaces the previous specified grouping expressions, if any.
	 * If no grouping expressions are specified, any previously
	 * added grouping expressions are simply removed.
	 *
	 * @param grouping list of zero or more grouping expressions
	 *
	 * @return the modified query
	 */
	AbstractQuery<T> groupBy(List<Expression<?>> grouping);

	/**
	 * Specify a restriction over the groups of the query.
	 * Replaces the previous having restriction(s), if any.
	 *
	 * @param restriction a simple or compound boolean expression
	 *
	 * @return the modified query
	 */
	AbstractQuery<T> having(Expression<Boolean> restriction);

	/**
	 * Specify restrictions over the groups of the query
	 * according the conjunction of the specified restriction
	 * predicates.
	 * Replaces the previously having added restriction(s), if any.
	 * If no restrictions are specified, any previously added
	 * restrictions are simply removed.
	 *
	 * @param restrictions zero or more restriction predicates
	 *
	 * @return the modified query
	 */
	AbstractQuery<T> having(Predicate... restrictions);

	/**
	 * Specify whether duplicate query results will be eliminated.
	 * A true value will cause duplicates to be eliminated.
	 * A false value will cause duplicates to be retained.
	 * If distinct has not been specified, duplicate results must
	 * be retained.
	 *
	 * @param distinct boolean value specifying whether duplicate
	 * results must be eliminated from the query result or
	 * whether they must be retained
	 *
	 * @return the modified query
	 */
	AbstractQuery<T> distinct(boolean distinct);

	/**
	 * Create a subquery of the query.
	 *
	 * @param type the subquery result type
	 *
	 * @return subquery
	 */
	<U> Subquery<U> subquery(Class<U> type);

	/**
	 * Return the query roots.  These are the roots that have
	 * been defined for the <code>CriteriaQuery</code> or <code>Subquery</code> itself,
	 * including any subquery roots defined as a result of
	 * correlation. Returns empty set if no roots have been defined.
	 * Modifications to the set do not affect the query.
	 *
	 * @return the set of query roots
	 */
	Set<Root<?>> getRoots();

	/**
	 * Return the selection of the query, or null if no selection
	 * has been set.
	 *
	 * @return selection item
	 */
	Selection<T> getSelection();

	/**
	 * Return the predicate that corresponds to the where clause
	 * restriction(s), or null if no restrictions have been
	 * specified.
	 *
	 * @return where clause predicate
	 */
	Predicate getRestriction();

	/**
	 * Return a list of the grouping expressions.  Returns empty
	 * list if no grouping expressions have been specified.
	 * Modifications to the list do not affect the query.
	 *
	 * @return the list of grouping expressions
	 */
	List<Expression<?>> getGroupList();

	/**
	 * Return the predicate that corresponds to the restriction(s)
	 * over the grouping items, or null if no restrictions have
	 * been specified.
	 *
	 * @return having clause predicate
	 */
	Predicate getGroupRestriction();

	/**
	 * Return whether duplicate query results must be eliminated or
	 * retained.
	 *
	 * @return boolean indicating whether duplicate query results
	 *         must be eliminated
	 */
	boolean isDistinct();

	/**
	 * Return the result type of the query or subquery.  If a result
	 * type was specified as an argument to the
	 * <code>createQuery</code> or <code>subquery</code> method, that
	 * type will be returned.  If the query was created using the
	 * <code>createTupleQuery</code> method, the result type is
	 * <code>Tuple</code>.  Otherwise, the result type is
	 * <code>Object</code>.
	 *
	 * @return result type
	 */
	Class<T> getResultType();
}


