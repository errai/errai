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

// $Id: OptimisticLockException.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;


/**
 * Thrown by the persistence provider when an optimistic locking conflict
 * occurs. This exception may be thrown as part of an API call, a flush or at
 * commit time. The current transaction, if one is active, will be marked for
 * rollback.
 *
 * @see EntityManager#find(Class, Object, LockModeType)
 * @see EntityManager#find(Class, Object, LockModeType, java.util.Map)
 * @see EntityManager#lock(Object, LockModeType)
 * @see EntityManager#lock(Object, LockModeType, java.util.Map)
 *
 * @since Java Persistence 1.0
 */
public class OptimisticLockException extends PersistenceException {

	/**
	 * The object that caused the exception
	 */
	Object entity;

	/**
	 * Constructs a new <code>OptimisticLockException</code> exception with
	 * <code>null</code> as its detail message.
	 */
	public OptimisticLockException() {
		super();
	}

	/**
	 * Constructs a new <code>OptimisticLockException</code> exception with the
	 * specified detail message.
	 *
	 * @param message
	 *            the detail message.
	 */
	public OptimisticLockException(String message) {
		super(message);
	}

	/**
	 * Constructs a new <code>OptimisticLockException</code> exception with the
	 * specified detail message and cause.
	 *
	 * @param message
	 *            the detail message.
	 * @param cause
	 *            the cause.
	 */
	public OptimisticLockException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new <code>OptimisticLockException</code> exception with the
	 * specified cause.
	 *
	 * @param cause
	 *            the cause.
	 */
	public OptimisticLockException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new <code>OptimisticLockException</code> exception with the
	 * specified entity.
	 *
	 * @param entity
	 *            the entity.
	 */
	public OptimisticLockException(Object entity) {
		this.entity = entity;
	}

	/**
	 * Constructs a new <code>OptimisticLockException</code> exception with the
	 * specified detail message, cause, and entity.
	 *
	 * @param message
	 *            the detail message.
	 * @param cause
	 *            the cause.
	 * @param entity
	 *            the entity.
	 */
	public OptimisticLockException(String message, Throwable cause, Object entity) {
		super(message, cause);
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
