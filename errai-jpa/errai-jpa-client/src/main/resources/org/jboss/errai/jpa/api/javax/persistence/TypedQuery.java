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

// $Id:$

package javax.persistence;

import java.util.Date;
import java.util.List;

/**
 * Interface used to control the execution of typed queries.
 *
 * @param <X> query result type
 * @see Query
 * @see Parameter
 * @since Java Persistence 2.0
 */
public interface TypedQuery<X> extends Query {
	/**
	 * Execute a SELECT query and return the query results
	 * as a typed List.
	 *
	 * @return a list of the results
	 *
	 * @throws IllegalStateException if called for a Java
	 * Persistence query language UPDATE or DELETE statement
	 * @throws QueryTimeoutException if the query execution exceeds
	 * the query timeout value set and only the statement is
	 * rolled back
	 * @throws TransactionRequiredException if a lock mode has
	 * been set and there is no transaction
	 * @throws PessimisticLockException if pessimistic locking
	 * fails and the transaction is rolled back
	 * @throws LockTimeoutException if pessimistic locking
	 * fails and only the statement is rolled back
	 * @throws PersistenceException if the query execution exceeds
	 * the query timeout value set and the transaction
	 * is rolled back
	 */
	@Override
  List<X> getResultList();

	/**
	 * Execute a SELECT query that returns a single result.
	 *
	 * @return the result
	 *
	 * @throws NoResultException if there is no result
	 * @throws NonUniqueResultException if more than one result
	 * @throws IllegalStateException if called for a Java
	 * Persistence query language UPDATE or DELETE statement
	 * @throws QueryTimeoutException if the query execution exceeds
	 * the query timeout value set and only the statement is
	 * rolled back
	 * @throws TransactionRequiredException if a lock mode has
	 * been set and there is no transaction
	 * @throws PessimisticLockException if pessimistic locking
	 * fails and the transaction is rolled back
	 * @throws LockTimeoutException if pessimistic locking
	 * fails and only the statement is rolled back
	 * @throws PersistenceException if the query execution exceeds
	 * the query timeout value set and the transaction
	 * is rolled back
	 */
	@Override
  X getSingleResult();

	/**
	 * Set the maximum number of results to retrieve.
	 *
	 * @param maxResult maximum number of results to retrieve
	 *
	 * @return the same query instance
	 *
	 * @throws IllegalArgumentException if the argument is negative
	 */
	@Override
  TypedQuery<X> setMaxResults(int maxResult);

	/**
	 * Set the position of the first result to retrieve.
	 *
	 * @param startPosition position of the first result,
	 * numbered from 0
	 *
	 * @return the same query instance
	 *
	 * @throws IllegalArgumentException if the argument is negative
	 */
	@Override
  TypedQuery<X> setFirstResult(int startPosition);

	/**
	 * Set a query property or hint. The hints elements may be used
	 * to specify query properties and hints. Properties defined by
	 * this specification must be observed by the provider.
	 * Vendor-specific hints that are not recognized by a provider
	 * must be silently ignored. Portable applications should not
	 * rely on the standard timeout hint. Depending on the database
	 * in use and the locking mechanisms used by the provider,
	 * this hint may or may not be observed.
	 *
	 * @param hintName name of property or hint
	 * @param value value for the property or hint
	 *
	 * @return the same query instance
	 *
	 * @throws IllegalArgumentException if the second argument is not
	 * valid for the implementation
	 */
	@Override
  TypedQuery<X> setHint(String hintName, Object value);

	/**
	 * Bind the value of a <code>Parameter</code> object.
	 *
	 * @param param parameter object
	 * @param value parameter value
	 *
	 * @return the same query instance
	 *
	 * @throws IllegalArgumentException if the parameter
	 * does not correspond to a parameter of the
	 * query
	 */
	@Override
  <T> TypedQuery<X> setParameter(Parameter<T> param, T value);

//	/**
//	 * Bind an instance of <code>java.util.Calendar</code> to a <code>Parameter</code> object.
//	 *
//	 * @param param parameter object
//	 * @param value parameter value
//	 * @param temporalType temporal type
//	 *
//	 * @return the same query instance
//	 *
//	 * @throws IllegalArgumentException if the parameter does not
//	 * correspond to a parameter of the query
//	 */
//	TypedQuery<X> setParameter(Parameter<Calendar> param,
//							   Calendar value,
//							   TemporalType temporalType);

	/**
	 * Bind an instance of <code>java.util.Date</code> to a <code>Parameter</code> object.
	 *
	 * @param param parameter object
	 * @param value parameter value
	 * @param temporalType temporal type
	 *
	 * @return the same query instance
	 *
	 * @throws IllegalArgumentException if the parameter does not
	 * correspond to a parameter of the query
	 */
	@Override
  TypedQuery<X> setParameter(Parameter<Date> param, Date value,
							   TemporalType temporalType);

	/**
	 * Bind an argument to a named parameter.
	 *
	 * @param name parameter name
	 * @param value parameter value
	 *
	 * @return the same query instance
	 *
	 * @throws IllegalArgumentException if the parameter name does
	 * not correspond to a parameter of the query or if
	 * the argument is of incorrect type
	 */
	@Override
  TypedQuery<X> setParameter(String name, Object value);

//	/**
//	 * Bind an instance of <code>java.util.Calendar</code> to a named parameter.
//	 *
//	 * @param name parameter name
//	 * @param value parameter value
//	 * @param temporalType temporal type
//	 *
//	 * @return the same query instance
//	 *
//	 * @throws IllegalArgumentException if the parameter name does
//	 * not correspond to a parameter of the query or if
//	 * the value argument is of incorrect type
//	 */
//	TypedQuery<X> setParameter(String name, Calendar value,
//							   TemporalType temporalType);

	/**
	 * Bind an instance of <code>java.util.Date</code> to a named parameter.
	 *
	 * @param name parameter name
	 * @param value parameter value
	 * @param temporalType temporal type
	 *
	 * @return the same query instance
	 *
	 * @throws IllegalArgumentException if the parameter name does
	 * not correspond to a parameter of the query or if
	 * the value argument is of incorrect type
	 */
	@Override
  TypedQuery<X> setParameter(String name, Date value,
							   TemporalType temporalType);

	/**
	 * Bind an argument to a positional parameter.
	 *
	 * @param position position
	 * @param value parameter value
	 *
	 * @return the same query instance
	 *
	 * @throws IllegalArgumentException if position does not
	 * correspond to a positional parameter of the
	 * query or if the argument is of incorrect type
	 */
	@Override
  TypedQuery<X> setParameter(int position, Object value);

//	/**
//	 * Bind an instance of <code>java.util.Calendar</code> to a positional
//	 * parameter.
//	 *
//	 * @param position position
//	 * @param value parameter value
//	 * @param temporalType temporal type
//	 *
//	 * @return the same query instance
//	 *
//	 * @throws IllegalArgumentException if position does not
//	 * correspond to a positional parameter of the query
//	 * or if the value argument is of incorrect type
//	 */
//	TypedQuery<X> setParameter(int position, Calendar value,
//							   TemporalType temporalType);

	/**
	 * Bind an instance of <code>java.util.Date</code> to a positional parameter.
	 *
	 * @param position position
	 * @param value parameter value
	 * @param temporalType temporal type
	 *
	 * @return the same query instance
	 *
	 * @throws IllegalArgumentException if position does not
	 * correspond to a positional parameter of the query
	 * or if the value argument is of incorrect type
	 */
	@Override
  TypedQuery<X> setParameter(int position, Date value,
							   TemporalType temporalType);

	/**
	 * Set the flush mode type to be used for the query execution.
	 * The flush mode type applies to the query regardless of the
	 * flush mode type in use for the entity manager.
	 *
	 * @param flushMode flush mode
	 *
	 * @return the same query instance
	 */
	@Override
  TypedQuery<X> setFlushMode(FlushModeType flushMode);

	/**
	 * Set the lock mode type to be used for the query execution.
	 *
	 * @param lockMode lock mode
	 *
	 * @return the same query instance
	 *
	 * @throws IllegalStateException if the query is found not to
	 * be a Java Persistence query language SELECT query
	 * or a Criteria API query
	 */
	@Override
  TypedQuery<X> setLockMode(LockModeType lockMode);
}
