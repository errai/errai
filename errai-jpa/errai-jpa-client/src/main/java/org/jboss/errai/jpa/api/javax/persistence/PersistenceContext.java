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

// $Id: PersistenceContext.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Expresses a dependency on a container-managed {@link EntityManager} and its
 * associated persistence context.
 *
 * @since Java Persistence 1.0
 */

@Target({TYPE, METHOD, FIELD})
@Retention(RUNTIME)
public @interface PersistenceContext {

    /**
     * (Optional) The name by which the entity manager is to be accessed in the
     * environment referencing context; not needed when dependency
     * injection is used.
     */
    String name() default "";

    /**
     * (Optional) The name of the persistence unit as defined in the
     * <code>persistence.xml</code> file. If the <code>unitName</code> element is
     * specified, the persistence unit for the entity manager that is
     * accessible in JNDI must have the same name.
     */
    String unitName() default "";

    /**
     * (Optional) Specifies whether a transaction-scoped persistence context
     * or an extended persistence context is to be used.
     */
    PersistenceContextType type() default PersistenceContextType.TRANSACTION;

    /**
     * (Optional) Properties for the container or persistence
     * provider.  Vendor specific properties may be included in this
     * set of properties.  Properties that are not recognized by
     * a vendor are ignored.
     */
    PersistenceProperty[] properties() default {};
}
