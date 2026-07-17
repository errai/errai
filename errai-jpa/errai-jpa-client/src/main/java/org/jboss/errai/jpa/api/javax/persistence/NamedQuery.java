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

// $Id: NamedQuery.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static javax.persistence.LockModeType.NONE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies a static, named query in the Java Persistence query language.
 * Query names are scoped to the persistence unit.
 * The <code>NamedQuery</code> annotation can be applied to an entity or mapped superclass.
 *
 * <p> The following is an example of the definition of a named query
 * in the Java Persistence query language:
 *
 * <pre>
 *    &#064;NamedQuery(
 *            name="findAllCustomersWithName",
 *            query="SELECT c FROM Customer c WHERE c.name LIKE :custName"
 *    )
 * </pre>
 *
 * <p> The following is an example of the use of a named query:
 *
 * <pre>
 *    &#064;PersistenceContext
 *    public EntityManager em;
 *    ...
 *    customers = em.createNamedQuery("findAllCustomersWithName")
 *            .setParameter("custName", "Smith")
 *            .getResultList();
 * </pre>
 *
 * @since Java Persistence 1.0
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface NamedQuery {

    /**
     * (Required) The name used to refer to the query with the {@link EntityManager}
     * methods that create query objects.
     */
    String name();

    /** (Required)
     * The query string in the Java Persistence query language.
     */
    String query();

    /**
     * (Optional) The lock mode type to use in query execution.  If a <code>lockMode</code>
     * other than <code>LockModeType.NONE</code> is specified, the query must be executed in
     * a transaction.
     * @since Java Persistence 2.0
     */
    LockModeType lockMode() default NONE;

    /** (Optional) Query properties and hints.  May include
     * vendor-specific query hints.
     */
    QueryHint[] hints() default {};
}
