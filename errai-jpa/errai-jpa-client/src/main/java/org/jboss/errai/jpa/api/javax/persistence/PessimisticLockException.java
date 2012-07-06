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

// $Id: PessimisticLockException.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

/**
 * Thrown by the persistence provider when an pessimistic locking conflict
 * occurs. This exception may be thrown as part of an API call, a flush or at
 * commit time. The current transaction, if one is active, will be marked for
 * rollback.
 *
 * @since Java Persistence 2.0
 */
public class PessimisticLockException extends PersistenceException {
	/**
	 * The object that caused the exception
	 */
	Object entity;

	/**
	 * Constructs a new <code>PessimisticLockException</code> exception
	 * with <code>null</code> as its detail message.
	 */
	public PessimisticLockException() {
		super();
	}

	/**
	 * Constructs a new <code>PessimisticLockException</code> exception
	 * with the specified detail message.
	 *
	 * @param message the detail message.
	 */
	public PessimisticLockException(String message) {
		super( message );
	}

	/**
	 * Constructs a new <code>PessimisticLockException</code> exception
	 * with the specified detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause the cause.
	 */
	public PessimisticLockException(String message, Throwable cause) {
		super( message, cause );
	}

	/**
	 * Constructs a new <code>PessimisticLockException</code> exception
	 * with the specified cause.
	 *
	 * @param cause the cause.
	 */
	public PessimisticLockException(Throwable cause) {
		super( cause );
	}

	/**
	 * Constructs a new <code>PessimisticLockException</code> exception
	 * with the specified entity.
	 *
	 * @param entity the entity.
	 */
	public PessimisticLockException(Object entity) {
		this.entity = entity;
	}

	/**
	 * Constructs a new <code>PessimisticLockException</code> exception
	 * with the specified detail message, cause, and entity.
	 *
	 * @param message the detail message.
	 * @param cause the cause.
	 * @param entity the entity.
	 */
	public PessimisticLockException(String message, Throwable cause, Object entity) {
		super( message, cause );
		this.entity = entity;
	}

	/**
	 * Returns the entity that caused this exception.
	 *
	 * @return the entity.
	 */
	public Object getEntity() {
		return this.entity;
	}
}