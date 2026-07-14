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

// $Id: FetchType.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

/**
 * Defines strategies for fetching data from the database.
 * The <code>EAGER</code> strategy is a requirement on the persistence
 * provider runtime that data must be eagerly fetched. The
 * <code>LAZY</code> strategy is a hint to the persistence provider
 * runtime that data should be fetched lazily when it is
 * first accessed. The implementation is permitted to eagerly
 * fetch data for which the <code>LAZY</code> strategy hint has been
 * specified.
 *
 * <pre>
 *   Example:
 *   &#064;Basic(fetch=LAZY)
 *   protected String getName() { return name; }
 * </pre>
 *
 * @see Basic
 * @see ElementCollection
 * @see ManyToMany
 * @see OneToMany
 * @see ManyToOne
 * @see OneToOne
 * @since Java Persistence 1.0
 */
public enum FetchType {

    /** Defines that data can be lazily fetched. */
    LAZY,

    /** Defines that data must be eagerly fetched. */
    EAGER
}
