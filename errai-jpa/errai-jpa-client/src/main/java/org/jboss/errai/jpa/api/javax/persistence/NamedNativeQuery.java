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

// $Id: NamedNativeQuery.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies a named native SQL query.
 * Query names are scoped to the persistence unit.
 * The <code>NamedNativeQuery</code> annotation can be applied to an
 * entity or mapped superclass.
 *
 * @since Java Persistence 1.0
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface NamedNativeQuery {

    /**
     * The name used to refer to the query with the {@link EntityManager}
     * methods that create query objects.
     */
    String name();

    /** The SQL query string. */
    String query();

    /** Query properties and hints.  (May include vendor-specific query hints.) */
    QueryHint[] hints() default {};

    /** The class of the result. */
    Class resultClass() default void.class;

    /** The name of a {@link SqlResultSetMapping}, as defined in metadata. */
    String resultSetMapping() default "";
}
