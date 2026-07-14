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

// $Id: RollbackException.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

/**
 * Thrown by the persistence provider when
 * {@link EntityTransaction#commit() EntityTransaction.commit()} fails.
 *
 * @see javax.persistence.EntityTransaction#commit()
 * @since Java Persistence 1.0
 */
public class RollbackException extends PersistenceException {
	/**
	 * Constructs a new <code>RollbackException</code> exception
	 * with <code>null</code> as its detail message.
	 */
	public RollbackException() {
		super();
	}

	/**
	 * Constructs a new <code>RollbackException</code> exception
	 * with the specified detail message.
	 *
	 * @param message the detail message.
	 */
	public RollbackException(String message) {
		super( message );
	}

	/**
	 * Constructs a new <code>RollbackException</code> exception
	 * with the specified detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause the cause.
	 */
	public RollbackException(String message, Throwable cause) {
		super( message, cause );
	}

	/**
	 * Constructs a new <code>RollbackException</code> exception
	 * with the specified cause.
	 *
	 * @param cause the cause.
	 */
	public RollbackException(Throwable cause) {
		super(cause);
	}
}
