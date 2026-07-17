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

// $Id: Cache.java 16130 2009-03-10 14:28:07Z hardy.ferentschik $

package javax.persistence;

/**
 * Used as the value of the
 * <code>javax.persistence.cache.storeMode</code> property to specify
 * the behavior when data is read from the database and when data is
 * committed into the database.
 *
 * @since Java Persistence 2.0
 */
public enum CacheStoreMode {

    /**
     * Insert/update entity data into cache when read
     * from database and when committed into database:
     * this is the default behavior. Does not force refresh
     * of already cached items when reading from database.
     */
    USE,

    /**
     * Don't insert into cache.
     */
    BYPASS,

    /**
     * Insert/update entity data into cache when read
     * from database and when committed into database.
     * Forces refresh of cache for items read from database.
     */
    REFRESH
}
