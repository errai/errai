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

// $Id: JoinColumns.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Defines mapping for composite foreign keys. This annotation
 * groups <code>JoinColumn</code> annotations for the same relationship.
 *
 * <p> When the <code>JoinColumns</code> annotation is used,
 * both the <code>name</code> and the <code>referencedColumnName</code> elements
 * must be specified in each such <code>JoinColumn</code> annotation.
 *
 * <pre>
 *
 *    Example:
 *    &#064;ManyToOne
 *    &#064;JoinColumns({
 *        &#064;JoinColumn(name="ADDR_ID", referencedColumnName="ID"),
 *        &#064;JoinColumn(name="ADDR_ZIP", referencedColumnName="ZIP")
 *    })
 *    public Address getAddress() { return address; }
 * </pre>
 *
 * @see JoinColumn
 *
 * @since Java Persistence 1.0
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface JoinColumns {

    /**
     * The join columns that map the relationship.
     */
    JoinColumn[] value();
}
