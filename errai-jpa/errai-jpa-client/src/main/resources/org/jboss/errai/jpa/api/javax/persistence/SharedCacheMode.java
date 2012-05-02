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

// $Id: SharedCacheMode.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

/**
 * Specifies how the provider must use a second-level cache for the
 * persistence unit.  Corresponds to the value of the <code>persistence.xml</code>
 * <code>shared-cache-mode</code> element, and returned as the result of
 * {@link javax.persistence.spi.PersistenceUnitInfo#getSharedCacheMode()}.
 *
 * @since Java Persistence 2.0
 */
public enum SharedCacheMode {
    /**
     * All entities and entity-related state and data are cached.
     */
    ALL,

    /**
     * Caching is disabled for the persistence unit.
     */
    NONE,

    /**
     * Caching is enabled for all entities for <code>Cacheable(true)</code>
     * is specified.  All other entities are not cached.
     */
    ENABLE_SELECTIVE, 

    /**
     * Caching is enabled for all entities except those for which
     * <code>Cacheable(false) is specified.  Entities for which
     * <code>Cacheable(false) is specified are not cached.
     */
    DISABLE_SELECTIVE,

    /**
     *
     * Caching behavior is undefined: provider-specific defaults may apply.
     */
    UNSPECIFIED
}
