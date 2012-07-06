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

// $Id: LockModeType.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

/**
 * Lock modes can be specified by means of passing a <code>LockModeType</code>
 * argument to one of the {@link javax.persistence.EntityManager} methods that take locks
 * (<code>lock</code>, <code>find</code>, or <code>refresh</code>) or
 * to the {@link Query#setLockMode Query.setLockMode()} or
 * {@link TypedQuery#setLockMode TypedQuery.setLockMode()} method.
 *
 * <p> Lock modes can be used to specify either optimistic or pessimistic locks.
 *
 * <p> Optimistic locks are specified using {@link
 * LockModeType#OPTIMISTIC LockModeType.OPTIMISTIC} and {@link
 * LockModeType#OPTIMISTIC_FORCE_INCREMENT
 * LockModeType.OPTIMISTIC_FORCE_INCREMENT}.  The lock mode type
 * values {@link LockModeType#READ LockModeType.READ} and
 * {@link LockModeType#WRITE LockModeType.WRITE} are
 * synonyms of <code>OPTIMISTIC</code> and
 * <code>OPTIMISTIC_FORCE_INCREMENT</code> respectively.  The latter
 * are to be preferred for new applications.
 *
 * <p> The semantics of requesting locks of type
 * <code>LockModeType.OPTIMISTIC</code> and
 * <code>LockModeType.OPTIMISTIC_FORCE_INCREMENT<code> are the
 * following.
 *
 * <p> If transaction T1 calls for a lock of type
 * <code>LockModeType.OPTIMISTIC</code> on a versioned object,
 * the entity manager must ensure that neither of the following
 * phenomena can occur:
 * <ul>
 *   <li> P1 (Dirty read): Transaction T1 modifies a row.
 * Another transaction T2 then reads that row and obtains
 * the modified value, before T1 has committed or rolled back.
 * Transaction T2 eventually commits successfully; it does not
 * matter whether T1 commits or rolls back and whether it does
 * so before or after T2 commits.
 *   <li>
 *   </li> P2 (Non-repeatable read): Transaction T1 reads a row.
 * Another transaction T2 then modifies or deletes that row,
 * before T1 has committed. Both transactions eventually commit
 * successfully.
 *   </li>
 * </ul>
 *
 * <p> Lock modes must always prevent the phenomena P1 and P2.
 *
 * <p> In addition, calling a lock of type
 * <code>LockModeType.OPTIMISTIC_FORCE_INCREMENT</code> on a versioned object,
 * will also force an update (increment) to the entity's version
 * column.
 *
 * <p> The persistence implementation is not required to support
 * the use of optimistic lock modes on non-versioned objects. When it
 * cannot support a such lock call, it must throw the {@link
 * PersistenceException}.
 *
 * <p>The lock modes {@link LockModeType#PESSIMISTIC_READ
 * LockModeType.PESSIMISTIC_READ}, {@link
 * LockModeType#PESSIMISTIC_WRITE LockModeType.PESSIMISTIC_WRITE}, and
 * {@link LockModeType#PESSIMISTIC_FORCE_INCREMENT
 * LockModeType.PESSIMISTIC_FORCE_INCREMENT} are used to immediately
 * obtain long-term database locks.
 *
 * <p> The semantics of requesting locks of type
 * <code>LockModeType.PESSIMISTIC_READ</code>, <code>LockModeType.PESSIMISTIC_WRITE</code>, and
 * <code>LockModeType.PESSIMISTIC_FORCE_INCREMENT</code> are the following.
 *
 * <p> If transaction T1 calls for a lock of type
 * <code>LockModeType.PESSIMISTIC_READ</code> or
 * <code>LockModeType.PESSIMISTIC_WRITE</code> on an object, the entity
 * manager must ensure that neither of the following phenomena can
 * occur:
 * <ul>
 * <li> P1 (Dirty read): Transaction T1 modifies a
 * row. Another transaction T2 then reads that row and obtains the
 * modified value, before T1 has committed or rolled back.
 *
 * <li> P2 (Non-repeatable read): Transaction T1 reads a row. Another
 * transaction T2 then modifies or deletes that row, before T1 has
 * committed or rolled back.
 * </ul>
 *
 * <p> A lock with <code>LockModeType.PESSIMISTIC_WRITE</code> can be obtained on
 * an entity instance to force serialization among transactions
 * attempting to update the entity data. A lock with
 * <code>LockModeType.PESSIMISTIC_READ</code> can be used to query data using
 * repeatable-read semantics without the need to reread the data at
 * the end of the transaction to obtain a lock, and without blocking
 * other transactions reading the data. A lock with
 * <code>LockModeType.PESSIMISTIC_WRITE</code> can be used when querying data and
 * there is a high likelihood of deadlock or update failure among
 * concurrent updating transactions.
 *
 * <p> The persistence implementation must support use of locks of type
 * <code>LockModeType.PESSIMISTIC_READ</code>
 * <code>LockModeType.PESSIMISTIC_WRITE</code> on a non-versioned entity as well as
 * on a versioned entity.
 *
 * <p> When the lock cannot be obtained, and the database locking
 * failure results in transaction-level rollback, the provider must
 * throw the {@link PessimisticLockException} and ensure that the JTA
 * transaction or <code>EntityTransaction</code> has been marked for rollback.
 *
 * <p> When the lock cannot be obtained, and the database locking
 * failure results in only statement-level rollback, the provider must
 * throw the {@link LockTimeoutException} (and must not mark the transaction
 * for rollback).
 *
 * @since Java Persistence 1.0
 *
 */
public enum LockModeType
{
    /**
     *  Synonymous with <code>OPTIMISTIC</code>.
     *  <code>OPTIMISTIC</code> is to be preferred for new
     *  applications.
     *
     */
    READ,

    /**
     *  Synonymous with <code>OPTIMISTIC_FORCE_INCREMENT</code>.
     *  <code>OPTIMISTIC_FORCE_IMCREMENT</code> is to be preferred for new
     *  applications.
     *
     */
    WRITE,

    /**
     * Optimistic lock.
     *
     * @since Java Persistence 2.0
     */
    OPTIMISTIC,

    /**
     * Optimistic lock, with version update.
     *
     * @since Java Persistence 2.0
     */
    OPTIMISTIC_FORCE_INCREMENT,

    /**
     *
     * Pessimistic read lock.
     *
     * @since Java Persistence 2.0
     */
    PESSIMISTIC_READ,

    /**
     * Pessimistic write lock.
     *
     * @since Java Persistence 2.0
     */
    PESSIMISTIC_WRITE,

    /**
     * Pessimistic write lock, with version update.
     *
     * @since Java Persistence 2.0
     */
    PESSIMISTIC_FORCE_INCREMENT,

    /**
     * No lock.
     *
     * @since Java Persistence 2.0
     */
    NONE
}
