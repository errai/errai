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

// $Id: NoResultException.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

/**
 * Thrown by the persistence provider when {@link
 * Query#getSingleResult Query.getSingleResult()} or {@link
 * TypedQuery#getSingleResult TypedQuery.getSingleResult()}is executed on a query
 * and there is no result to return.  This exception will not cause
 * the current transaction, if one is active, to be marked for
 * rollback.
 *
 * @see Query#getSingleResult()
 * @see TypedQuery#getSingleResult()
 *
 * @since Java Persistence 1.0
 */
public class NoResultException extends PersistenceException {

	/**
	 * Constructs a new <code>NoResultException</code> exception with
	 * <code>null</code> as its detail message.
	 */
	public NoResultException() {
		super();
	}

	/**
	 * Constructs a new <code>NoResultException</code> exception with the
	 * specified detail message.
	 *
	 * @param message
	 *            the detail message.
	 */
	public NoResultException(String message) {
		super(message);
	}
}
