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

// $Id: Basic.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static javax.persistence.FetchType.EAGER;

/**
 * The simplest type of mapping to a database column. The
 * <code>Basic</code> annotation can be applied to a persistent
 * property or instance variable of any of the following types: Java
 * primitive types, wrappers of the primitive types, <code>String</code>,
 * <code>java.math.BigInteger</code>,
 * <code>java.math.BigDecimal</code>,
 * <code>java.util.Date</code>,
 * <code>java.util.Calendar</code>,
 * <code>java.sql.Date</code>,
 * <code>java.sql.Time</code>,
 * <code>java.sql.Timestamp</code>, <code>byte[]</code>, <code>Byte[]</code>,
 * <code>char[]</code>, <code>Character[]</code>, enums, and any other type that
 * implements <code>java.io.Serializable</code>.
 *
 * <p> The use of the <code>Basic</code> annotation is optional for
 * persistent fields and properties of these types.  If the
 * <code>Basic</code> annotation is not specified for such a field or
 * property, the default values of the <code>Basic</code> annotation
 * will apply.
 *
 * <pre>
 *    Example 1:
 *
 *    &#064;Basic
 *    protected String name;
 *
 *    Example 2:
 *
 *    &#064;Basic(fetch=LAZY)
 *    protected String getName() { return name; }
 *
 * </pre>
 * @since Java Persistence 1.0
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Basic {

    /**
     * (Optional) Defines whether the value of the field or property should
     * be lazily loaded or must be eagerly fetched. The <code>EAGER</code>
     * strategy is a requirement on the persistence provider runtime
     * that the value must be eagerly fetched.  The <code>LAZY</code>
     * strategy is a hint to the persistence provider runtime.
     * If not specified, defaults to <code>EAGER</code>.
     */
    FetchType fetch() default EAGER;

    /**
     * (Optional) Defines whether the value of the field or property may be null.
     * This is a hint and is disregarded for primitive types; it may
     * be used in schema generation.
     * If not specified, defaults to <code>true</code>.
     */
    boolean optional() default true;
}
