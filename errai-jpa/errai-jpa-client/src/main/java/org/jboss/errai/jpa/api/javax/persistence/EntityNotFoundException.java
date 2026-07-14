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

// $Id: EntityNotFoundException.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

/**
 * Thrown by the persistence provider when an entity reference obtained by
 * {@link EntityManager#getReference EntityManager.getReference}
 * is accessed but the entity does not exist. Thrown when
 * {@link EntityManager#refresh EntityManager.refresh} is called and the
 * object no longer exists in the database.
 * Thrown when {@link EntityManager#lock EntityManager.lock} is used with
 * pessimistic locking is used and the entity no longer exists in the database.
 * <p> The current transaction, if one is active, will be marked for rollback.
 *
 * @see EntityManager#getReference(Class,Object)
 * @see EntityManager#refresh(Object)
 * @see EntityManager#refresh(Object, LockModeType)
 * @see EntityManager#refresh(Object, java.util.Map)
 * @see EntityManager#refresh(Object, LockModeType, java.util.Map)
 * @see EntityManager#lock(Object, LockModeType)
 * @see EntityManager#lock(Object, LockModeType, java.util.Map)
 *
 * @since Java Persistence 1.0
 */
public class EntityNotFoundException extends PersistenceException {

	/**
	 * Constructs a new <code>EntityNotFoundException</code> exception with
	 * <code>null</code> as its detail message.
	 */
	public EntityNotFoundException() {
		super();
	}

	/**
	 * Constructs a new <code>EntityNotFoundException</code> exception with the
	 * specified detail message.
	 *
	 * @param message
	 *            the detail message.
	 */
	public EntityNotFoundException(String message) {
		super(message);
	}

}
