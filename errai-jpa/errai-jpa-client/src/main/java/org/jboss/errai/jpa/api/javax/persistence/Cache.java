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

// $Id: Cache.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

/**
 * Interface used to interact with the second-level cache.
 * If a cache is not in use, the methods of this interface have
 * no effect, except for <code>contains</code>, which returns false.
 *
 * @since Java Persistence 2.0
 */
public interface Cache {

    /**
     * Whether the cache contains data for the given entity.
     * @param cls  entity class
     * @param primaryKey  primary key
     * @return boolean indicating whether the entity is in the cache
     */
    public boolean contains(Class cls, Object primaryKey);

    /**
     * Remove the data for the given entity from the cache.
     * @param cls  entity class
     * @param primaryKey  primary key
     */
    public void evict(Class cls, Object primaryKey);

    /**
     * Remove the data for entities of the specified class (and its
     * subclasses) from the cache.
     * @param cls  entity class
     */
    public void evict(Class cls);

    /**
     * Clear the cache.
     */
    public void evictAll();
}
