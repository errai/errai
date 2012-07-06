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

// $Id: QueryTimeoutException.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

/**
 * Thrown by the persistence provider when a query times out
 * and only the statement is rolled back.
 * The current transaction, if one is active, will be not
 * be marked for rollback.
 *
 * @since Java Persistence 2.0
 */
public class QueryTimeoutException extends PersistenceException {
	/**
	 * The query object that caused the exception
	 */
	Query query;

	/**
	 * Constructs a new <code>QueryTimeoutException</code> exception
	 * with <code>null</code> as its detail message.
	 */
	public QueryTimeoutException() {
		super();
	}

	/**
	 * Constructs a new <code>QueryTimeoutException</code> exception
	 * with the specified detail message.
	 *
	 * @param message the detail message.
	 */
	public QueryTimeoutException(String message) {
		super( message );
	}

	/**
	 * Constructs a new <code>QueryTimeoutException</code> exception
	 * with the specified detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause the cause.
	 */
	public QueryTimeoutException(String message, Throwable cause) {
		super( message, cause );
	}

	/**
	 * Constructs a new <code>QueryTimeoutException</code> exception
	 * with the specified cause.
	 *
	 * @param cause the cause.
	 */
	public QueryTimeoutException(Throwable cause) {
		super( cause );
	}


	/**
	 * Constructs a new <code>QueryTimeoutException</code> exception
	 * with the specified query.
	 *
	 * @param query the query.
	 */
	public QueryTimeoutException(Query query) {
		this.query = query;
	}

	/**
	 * Constructs a new <code>QueryTimeoutException</code> exception
	 * with the specified detail message, cause, and query.
	 *
	 * @param message the detail message.
	 * @param cause the cause.
	 * @param query the query.
	 */
	public QueryTimeoutException(String message, Throwable cause, Query query) {
		super( message, cause );
		this.query = query;
	}

	/**
	 * Returns the query that caused this exception.
	 *
	 * @return the query.
	 */
	public Query getQuery() {
		return this.query;
	}
}
