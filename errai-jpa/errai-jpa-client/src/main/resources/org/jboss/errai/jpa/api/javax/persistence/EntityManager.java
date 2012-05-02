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

// $Id: EntityManager.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

import java.util.Map;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

/**
 * Interface used to interact with the persistence context.
 *
 * <p> An <code>EntityManager</code> instance is associated with
 * a persistence context. A persistence context is a set of entity
 * instances in which for any persistent entity identity there is
 * a unique entity instance. Within the persistence context, the
 * entity instances and their lifecycle are managed.
 * The <code>EntityManager</code> API is used
 * to create and remove persistent entity instances, to find entities
 * by their primary key, and to query over entities.
 *
 * <p> The set of entities that can be managed by a given
 * <code>EntityManager</code> instance is defined by a persistence
 * unit. A persistence unit defines the set of all classes that are
 * related or grouped by the application, and which must be
 * colocated in their mapping to a single database.
 *
 * @see Query
 * @see TypedQuery
 * @see CriteriaQuery
 * @see PersistenceContext
 *
 * @since Java Persistence 1.0
 */
public interface EntityManager {

    /**
     * Make an instance managed and persistent.
     * @param entity  entity instance
     * @throws EntityExistsException if the entity already exists.
     * (If the entity already exists, the <code>EntityExistsException</code> may
     * be thrown when the persist operation is invoked, or the
     * <code>EntityExistsException</code> or another <code>PersistenceException</code> may be
     * thrown at flush or commit time.)
     * @throws IllegalArgumentException if the instance is not an
     *         entity
     * @throws TransactionRequiredException if invoked on a
     *         container-managed entity manager of type
     *         <code>PersistenceContextType.TRANSACTION</code> and there is
     *         no transaction
     */
    public void persist(Object entity);

    /**
     * Merge the state of the given entity into the
     * current persistence context.
     * @param entity  entity instance
     * @return the managed instance that the state was merged to
     * @throws IllegalArgumentException if instance is not an
     *         entity or is a removed entity
     * @throws TransactionRequiredException if invoked on a
     *         container-managed entity manager of type
     *         <code>PersistenceContextType.TRANSACTION</code> and there is
     *         no transaction
     */
    public <T> T merge(T entity);

    /**
     * Remove the entity instance.
     * @param entity  entity instance
     * @throws IllegalArgumentException if the instance is not an
     *         entity or is a detached entity
     * @throws TransactionRequiredException if invoked on a
     *         container-managed entity manager of type
     *         <code>PersistenceContextType.TRANSACTION</code> and there is
     *         no transaction
     */
    public void remove(Object entity);

    /**
     * Find by primary key.
     * Search for an entity of the specified class and primary key.
     * If the entity instance is contained in the persistence context,
     * it is returned from there.
     * @param entityClass  entity class
     * @param primaryKey  primary key
     * @return the found entity instance or null if the entity does
     *         not exist
     * @throws IllegalArgumentException if the first argument does
     *         not denote an entity type or the second argument is
     *         is not a valid type for that entitys primary key or
     *         is null
     */
    public <T> T find(Class<T> entityClass, Object primaryKey);

    /**
     * Find by primary key, using the specified properties.
     * Search for an entity of the specified class and primary key.
     * If the entity instance is contained in the persistence
     * context, it is returned from there.
     * If a vendor-specific property or hint is not recognized,
     * it is silently ignored.
     * @param entityClass  entity class
     * @param primaryKey   primary key
     * @param properties  standard and vendor-specific properties
     *        and hints
     * @return the found entity instance or null if the entity does
     *         not exist
     * @throws IllegalArgumentException if the first argument does
     *         not denote an entity type or the second argument is
     *         is not a valid type for that entitys primary key or
     *         is null
     * @since Java Persistence 2.0
     */
    public <T> T find(Class<T> entityClass, Object primaryKey,
                      Map<String, Object> properties);

    /**
     * Find by primary key and lock.
     * Search for an entity of the specified class and primary key
     * and lock it with respect to the specified lock type.
     * If the entity instance is contained in the persistence context,
     * it is returned from there, and the effect of this method is
     * the same as if the lock method had been called on the entity.
     * <p> If the entity is found within the persistence context and the
     * lock mode type is pessimistic and the entity has a version
     * attribute, the persistence provider must perform optimistic
     * version checks when obtaining the database lock.  If these
     * checks fail, the <code>OptimisticLockException</code> will be thrown.
     * <p>If the lock mode type is pessimistic and the entity instance
     * is found but cannot be locked:
     * <ul>
     * <li> the <code>PessimisticLockException</code> will be thrown if the database
     *    locking failure causes transaction-level rollback
     * <li> the <code>LockTimeoutException</code> will be thrown if the database
     *    locking failure causes only statement-level rollback
     * </ul>
     * @param entityClass  entity class
     * @param primaryKey  primary key
     * @param lockMode  lock mode
     * @return the found entity instance or null if the entity does
     *         not exist
     * @throws IllegalArgumentException if the first argument does
     *         not denote an entity type or the second argument is
     *         not a valid type for that entity's primary key or
     *         is null
     * @throws TransactionRequiredException if there is no
     *         transaction and a lock mode other than NONE is
     *         specified
     * @throws OptimisticLockException if the optimistic version
     *         check fails
     * @throws PessimisticLockException if pessimistic locking
     *         fails and the transaction is rolled back
     * @throws LockTimeoutException if pessimistic locking fails and
     *         only the statement is rolled back
     * @throws PersistenceException if an unsupported lock call
     *         is made
     * @since Java Persistence 2.0
     */
    public <T> T find(Class<T> entityClass, Object primaryKey,
                      LockModeType lockMode);

    /**
     * Find by primary key and lock, using the specified properties.
     * Search for an entity of the specified class and primary key
     * and lock it with respect to the specified lock type.
     * If the entity instance is contained in the persistence context,
     * it is returned from there.
     * <p> If the entity is found
     * within the persistence context and the lock mode type
     * is pessimistic and the entity has a version attribute, the
     * persistence provider must perform optimistic version checks
     * when obtaining the database lock.  If these checks fail,
     * the <code>OptimisticLockException</code> will be thrown.
     * <p>If the lock mode type is pessimistic and the entity instance
     * is found but cannot be locked:
     * <ul>
     * <li> the <code>PessimisticLockException</code> will be thrown if the database
     *    locking failure causes transaction-level rollback
     * <li> the <code>LockTimeoutException</code> will be thrown if the database
     *    locking failure causes only statement-level rollback
     * </ul>
     * <p>If a vendor-specific property or hint is not recognized,
     * it is silently ignored.
     * <p>Portable applications should not rely on the standard timeout
     * hint. Depending on the database in use and the locking
     * mechanisms used by the provider, the hint may or may not
     * be observed.
     * @param entityClass  entity class
     * @param primaryKey  primary key
     * @param lockMode  lock mode
     * @param properties  standard and vendor-specific properties
     *        and hints
     * @return the found entity instance or null if the entity does
     *         not exist
     * @throws IllegalArgumentException if the first argument does
     *         not denote an entity type or the second argument is
     *         not a valid type for that entity's primary key or
     *         is null
     * @throws TransactionRequiredException if there is no
     *         transaction and a lock mode other than <code>NONE</code> is
     *         specified
     * @throws OptimisticLockException if the optimistic version
     *         check fails
     * @throws PessimisticLockException if pessimistic locking
     *         fails and the transaction is rolled back
     * @throws LockTimeoutException if pessimistic locking fails and
     *         only the statement is rolled back
     * @throws PersistenceException if an unsupported lock call
     *         is made
     * @since Java Persistence 2.0
     */
    public <T> T find(Class<T> entityClass, Object primaryKey,
                      LockModeType lockMode,
                      Map<String, Object> properties);

    /**
     * Get an instance, whose state may be lazily fetched.
     * If the requested instance does not exist in the database,
     * the <code>EntityNotFoundException</code> is thrown when the instance
     * state is first accessed. (The persistence provider runtime is
     * permitted to throw the <code>EntityNotFoundException</code> when
     * <code>getReference</code> is called.)
     * The application should not expect that the instance state will
     * be available upon detachment, unless it was accessed by the
     * application while the entity manager was open.
     * @param entityClass  entity class
     * @param primaryKey  primary key
     * @return the found entity instance
     * @throws IllegalArgumentException if the first argument does
     *         not denote an entity type or the second argument is
     *         not a valid type for that entitys primary key or
     *         is null
     * @throws EntityNotFoundException if the entity state
     *         cannot be accessed
     */
    public <T> T getReference(Class<T> entityClass,
                                  Object primaryKey);

    /**
     * Synchronize the persistence context to the
     * underlying database.
     * @throws TransactionRequiredException if there is
     *         no transaction
     * @throws PersistenceException if the flush fails
     */
    public void flush();

    /**
     * Set the flush mode that applies to all objects contained
     * in the persistence context.
     * @param flushMode  flush mode
     */
    public void setFlushMode(FlushModeType flushMode);

    /**
     * Get the flush mode that applies to all objects contained
     * in the persistence context.
     * @return flushMode
     */
    public FlushModeType getFlushMode();

    /**
     * Lock an entity instance that is contained in the persistence
     * context with the specified lock mode type.
     * <p>If a pessimistic lock mode type is specified and the entity
     * contains a version attribute, the persistence provider must
     * also perform optimistic version checks when obtaining the
     * database lock.  If these checks fail, the
     * <code>OptimisticLockException</code> will be thrown.
     * <p>If the lock mode type is pessimistic and the entity instance
     * is found but cannot be locked:
     * <ul>
     * <li> the <code>PessimisticLockException</code> will be thrown if the database
     *    locking failure causes transaction-level rollback
     * <li> the <code>LockTimeoutException</code> will be thrown if the database
     *    locking failure causes only statement-level rollback
     * </ul>
     * @param entity  entity instance
     * @param lockMode  lock mode
     * @throws IllegalArgumentException if the instance is not an
     *         entity or is a detached entity
     * @throws TransactionRequiredException if there is no
     *         transaction
     * @throws EntityNotFoundException if the entity does not exist
     *         in the database when pessimistic locking is
     *         performed
     * @throws OptimisticLockException if the optimistic version
     *         check fails
     * @throws PessimisticLockException if pessimistic locking fails
     *         and the transaction is rolled back
     * @throws LockTimeoutException if pessimistic locking fails and
     *         only the statement is rolled back
     * @throws PersistenceException if an unsupported lock call
     *         is made
     */
    public void lock(Object entity, LockModeType lockMode);

    /**
     * Lock an entity instance that is contained in the persistence
     * context with the specified lock mode type and with specified
     * properties.
     * <p>If a pessimistic lock mode type is specified and the entity
     * contains a version attribute, the persistence provider must
     * also perform optimistic version checks when obtaining the
     * database lock.  If these checks fail, the
     * <code>OptimisticLockException</code> will be thrown.
     * <p>If the lock mode type is pessimistic and the entity instance
     * is found but cannot be locked:
     * <ul>
     * <li> the <code>PessimisticLockException</code> will be thrown if the database
     *    locking failure causes transaction-level rollback
     * <li> the <code>LockTimeoutException</code> will be thrown if the database
     *    locking failure causes only statement-level rollback
     * </ul>
     * <p>If a vendor-specific property or hint is not recognized,
     * it is silently ignored.
     * <p>Portable applications should not rely on the standard timeout
     * hint. Depending on the database in use and the locking
     * mechanisms used by the provider, the hint may or may not
     * be observed.
     * @param entity  entity instance
     * @param lockMode  lock mode
     * @param properties  standard and vendor-specific properties
     *        and hints
     * @throws IllegalArgumentException if the instance is not an
     *         entity or is a detached entity
     * @throws TransactionRequiredException if there is no
     *         transaction
     * @throws EntityNotFoundException if the entity does not exist
     *         in the database when pessimistic locking is
     *         performed
     * @throws OptimisticLockException if the optimistic version
     *         check fails
     * @throws PessimisticLockException if pessimistic locking fails
     *         and the transaction is rolled back
     * @throws LockTimeoutException if pessimistic locking fails and
     *         only the statement is rolled back
     * @throws PersistenceException if an unsupported lock call
     *         is made
     * @since Java Persistence 2.0
     */
    public void lock(Object entity, LockModeType lockMode,
                     Map<String, Object> properties);

    /**
     * Refresh the state of the instance from the database,
     * overwriting changes made to the entity, if any.
     * @param entity  entity instance
     * @throws IllegalArgumentException if the instance is not
     *         an entity or the entity is not managed
     * @throws TransactionRequiredException if invoked on a
     *         container-managed entity manager of type
     *         <code>PersistenceContextType.TRANSACTION</code> and there is
     *         no transaction
     * @throws EntityNotFoundException if the entity no longer
     *         exists in the database
     */
    public void refresh(Object entity);

    /**
     * Refresh the state of the instance from the database, using
     * the specified properties, and overwriting changes made to
     * the entity, if any.
     * <p> If a vendor-specific property or hint is not recognized,
     * it is silently ignored.
     * @param entity  entity instance
     * @param properties  standard and vendor-specific properties
     *        and hints
     * @throws IllegalArgumentException if the instance is not
     *         an entity or the entity is not managed
     * @throws TransactionRequiredException if invoked on a
     *         container-managed entity manager of type
     *         <code>PersistenceContextType.TRANSACTION</code> and there is
     *         no transaction
     * @throws EntityNotFoundException if the entity no longer
     *         exists in the database
     * @since Java Persistence 2.0
     */
    public void refresh(Object entity,
                            Map<String, Object> properties);

    /**
     * Refresh the state of the instance from the database,
     * overwriting changes made to the entity, if any, and
     * lock it with respect to given lock mode type.
     * <p>If the lock mode type is pessimistic and the entity instance
     * is found but cannot be locked:
     * <ul>
     * <li> the <code>PessimisticLockException</code> will be thrown if the database
     *    locking failure causes transaction-level rollback
     * <li> the <code>LockTimeoutException</code> will be thrown if the
     *    database locking failure causes only statement-level
     *    rollback.
     * </ul>
     * @param entity  entity instance
     * @param lockMode  lock mode
     * @throws IllegalArgumentException if the instance is not
     *         an entity or the entity is not managed
     * @throws TransactionRequiredException if there is no
     *         transaction and if invoked on a container-managed
     *         <code>EntityManager</code> instance with
     *         <code>PersistenceContextType.TRANSACTION</code> or with a lock mode
     *         other than <code>NONE</code>
     * @throws EntityNotFoundException if the entity no longer exists
     *         in the database
     * @throws PessimisticLockException if pessimistic locking fails
     *         and the transaction is rolled back
     * @throws LockTimeoutException if pessimistic locking fails and
     *         only the statement is rolled back
     * @throws PersistenceException if an unsupported lock call
     *         is made
     * @since Java Persistence 2.0
     */
    public void refresh(Object entity, LockModeType lockMode);

    /**
     * Refresh the state of the instance from the database,
     * overwriting changes made to the entity, if any, and
     * lock it with respect to given lock mode type and with
     * specified properties.
     * <p>If the lock mode type is pessimistic and the entity instance
     * is found but cannot be locked:
     * <ul>
     * <li> the <code>PessimisticLockException</code> will be thrown if the database
     *    locking failure causes transaction-level rollback
     * <li> the <code>LockTimeoutException</code> will be thrown if the database
     *    locking failure causes only statement-level rollback
     * </ul>
     * <p>If a vendor-specific property or hint is not recognized,
     *    it is silently ignored.
     * <p>Portable applications should not rely on the standard timeout
     * hint. Depending on the database in use and the locking
     * mechanisms used by the provider, the hint may or may not
     * be observed.
     * @param entity  entity instance
     * @param lockMode  lock mode
     * @param properties  standard and vendor-specific properties
     *        and hints
     * @throws IllegalArgumentException if the instance is not
     *         an entity or the entity is not managed
     * @throws TransactionRequiredException if there is no
     *         transaction and if invoked on a container-managed
     *         <code>EntityManager</code> instance with
     *         <code>PersistenceContextType.TRANSACTION</code> or with a lock mode
     *         other than <code>NONE</code>
     * @throws EntityNotFoundException if the entity no longer exists
     *         in the database
     * @throws PessimisticLockException if pessimistic locking fails
     *         and the transaction is rolled back
     * @throws LockTimeoutException if pessimistic locking fails and
     *         only the statement is rolled back
     * @throws PersistenceException if an unsupported lock call
     *         is made
     * @since Java Persistence 2.0
     */
    public void refresh(Object entity, LockModeType lockMode,
                        Map<String, Object> properties);

    /**
     * Clear the persistence context, causing all managed
     * entities to become detached. Changes made to entities that
     * have not been flushed to the database will not be
     * persisted.
     */
    public void clear();

    /**
     * Remove the given entity from the persistence context, causing
     * a managed entity to become detached.  Unflushed changes made
     * to the entity if any (including removal of the entity),
     * will not be synchronized to the database.  Entities which
     * previously referenced the detached entity will continue to
     * reference it.
     * @param entity  entity instance
     * @throws IllegalArgumentException if the instance is not an
     *         entity
     * @since Java Persistence 2.0
     */
    public void detach(Object entity);

    /**
     * Check if the instance is a managed entity instance belonging
     * to the current persistence context.
     * @param entity  entity instance
     * @return boolean indicating if entity is in persistence context
     * @throws IllegalArgumentException if not an entity
     */
    public boolean contains(Object entity);

    /**
     * Get the current lock mode for the entity instance.
     * @param entity  entity instance
     * @return lock mode
     * @throws TransactionRequiredException if there is no
     *         transaction
     * @throws IllegalArgumentException if the instance is not a
     *         managed entity and a transaction is active
     * @since Java Persistence 2.0
     */
    public LockModeType getLockMode(Object entity);

    /**
     * Set an entity manager property or hint.
     * If a vendor-specific property or hint is not recognized, it is
     * silently ignored.
     * @param propertyName name of property or hint
     * @param value  value for property or hint
     * @throws IllegalArgumentException if the second argument is
     *         not valid for the implementation
     * @since Java Persistence 2.0
     */
    public void setProperty(String propertyName, Object value);

    /**
     * Get the properties and hints and associated values that are in effect
     * for the entity manager. Changing the contents of the map does
     * not change the configuration in effect.
     * @return map of properties and hints in effect for entity manager
     * @since Java Persistence 2.0
     */
    public Map<String, Object> getProperties();

    /**
     * Create an instance of <code>Query</code> for executing a
     * Java Persistence query language statement.
     * @param qlString a Java Persistence query string
     * @return the new query instance
     * @throws IllegalArgumentException if the query string is
     *         found to be invalid
     */
    public Query createQuery(String qlString);

    /**
     * Create an instance of <code>TypedQuery</code> for executing a
     * criteria query.
     * @param criteriaQuery  a criteria query object
     * @return the new query instance
     * @throws IllegalArgumentException if the criteria query is
     *         found to be invalid
     * @since Java Persistence 2.0
     */
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery);

    /**
     * Create an instance of <code>TypedQuery</code> for executing a
     * Java Persistence query language statement.
     * The select list of the query must contain only a single
     * item, which must be assignable to the type specified by
     * the <code>resultClass</code> argument.
     * @param qlString a Java Persistence query string
     * @param resultClass the type of the query result
     * @return the new query instance
     * @throws IllegalArgumentException if the query string is found
     *         to be invalid or if the query result is found to
     *         not be assignable to the specified type
     * @since Java Persistence 2.0
     */
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass);

    /**
     * Create an instance of <code>Query</code> for executing a named query
     * (in the Java Persistence query language or in native SQL).
     * @param name the name of a query defined in metadata
     * @return the new query instance
     * @throws IllegalArgumentException if a query has not been
     *         defined with the given name or if the query string is
     *         found to be invalid
     */
    public Query createNamedQuery(String name);

    /**
     * Create an instance of <code>TypedQuery</code> for executing a
     * Java Persistence query language named query.
     * The select list of the query must contain only a single
     * item, which must be assignable to the type specified by
     * the <code>resultClass</code> argument.
     * @param name the name of a query defined in metadata
     * @param resultClass the type of the query result
     * @return the new query instance
     * @throws IllegalArgumentException if a query has not been
     *         defined with the given name or if the query string is
     *         found to be invalid or if the query result is found to
     *         not be assignable to the specified type
     * @since Java Persistence 2.0
     */
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass);

    /**
     * Create an instance of <code>Query</code> for executing
     * a native SQL statement, e.g., for update or delete.
     * @param sqlString a native SQL query string
     * @return the new query instance
     */
    public Query createNativeQuery(String sqlString);

    /**
     * Create an instance of <code>Query</code> for executing
     * a native SQL query.
     * @param sqlString a native SQL query string
     * @param resultClass the class of the resulting instance(s)
     * @return the new query instance
     */
    public Query createNativeQuery(String sqlString, Class resultClass);

    /**
     * Create an instance of <code>Query</code> for executing
     * a native SQL query.
     * @param sqlString a native SQL query string
     * @param resultSetMapping the name of the result set mapping
     * @return the new query instance
     */
    public Query createNativeQuery(String sqlString, String resultSetMapping);

    /**
     * Indicate to the entity manager that a JTA transaction is
     * active. This method should be called on a JTA application
     * managed entity manager that was created outside the scope
     * of the active transaction to associate it with the current
     * JTA transaction.
     * @throws TransactionRequiredException if there is
     *         no transaction
     */
    public void joinTransaction();

    /**
     * Return an object of the specified type to allow access to the
     * provider-specific API.   If the provider's <code>EntityManager</code>
     * implementation does not support the specified class, the
     * <code>PersistenceException</code> is thrown.
     * @param cls  the class of the object to be returned.  This is
     * normally either the underlying <code>EntityManager</code> implementation
     * class or an interface that it implements.
     * @return an instance of the specified class
     * @throws PersistenceException if the provider does not
     *         support the call
     * @since Java Persistence 2.0
     */
    public <T> T unwrap(Class<T> cls);

    /**
     * Return the underlying provider object for the <code>EntityManager</code>,
     * if available. The result of this method is implementation
     * specific. The <code>unwrap</code> method is to be preferred for new
     * applications.
     * @return underlying provider object for EntityManager
     */
    public Object getDelegate();

    /**
     * Close an application-managed entity manager.
     * After the close method has been invoked, all methods
     * on the <code>EntityManager</code> instance and any
     * <code>Query</code> and <code>TypedQuery</code>
     * objects obtained from it will throw the <code>IllegalStateException</code>
     * except for <code>getProperties</code>,
     * <code>getTransaction</code>, and <code>isOpen</code> (which will return false).
     * If this method is called when the entity manager is
     * associated with an active transaction, the persistence
     * context remains managed until the transaction completes.
     * @throws IllegalStateException if the entity manager
     *         is container-managed
     */
    public void close();

    /**
     * Determine whether the entity manager is open.
     * @return true until the entity manager has been closed
     */
    public boolean isOpen();

    /**
     * Return the resource-level <code>EntityTransaction</code> object.
     * The <code>EntityTransaction</code> instance may be used serially to
     * begin and commit multiple transactions.
     * @return EntityTransaction instance
     * @throws IllegalStateException if invoked on a JTA
     *         entity manager
     */
    public EntityTransaction getTransaction();

    /**
     * Return the entity manager factory for the entity manager.
     * @return EntityManagerFactory instance
     * @throws IllegalStateException if the entity manager has
     *         been closed
     * @since Java Persistence 2.0
     */
    public EntityManagerFactory getEntityManagerFactory();

    /**
     * Return an instance of <code>CriteriaBuilder</code> for the creation of
     * <code>CriteriaQuery</code> objects.
     * @return CriteriaBuilder instance
     * @throws IllegalStateException if the entity manager has
     *         been closed
     * @since Java Persistence 2.0
     */
    public CriteriaBuilder getCriteriaBuilder();

    /**
     * Return an instance of <code>Metamodel</code> interface for access to the
     * metamodel of the persistence unit.
     * @return Metamodel instance
     * @throws IllegalStateException if the entity manager has
     *         been closed
     * @since Java Persistence 2.0
     */
    public Metamodel getMetamodel();
}