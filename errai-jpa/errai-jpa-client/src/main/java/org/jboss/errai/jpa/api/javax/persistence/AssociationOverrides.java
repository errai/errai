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

// $Id: AssociationOverrides.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used to override mappings of multiple relationship properties or fields.
 *
 * <pre>
 *
 *    Example:
 *
 *    &#064;MappedSuperclass
 *    public class Employee {
 *
 *        &#064;Id protected Integer id;
 *        &#064;Version protected Integer version;
 *        &#064;ManyToOne protected Address address;
 *        &#064;OneToOne protected Locker locker;
 *
 *        public Integer getId() { ... }
 *        public void setId(Integer id) { ... }
 *        public Address getAddress() { ... }
 *        public void setAddress(Address address) { ... }
 *        public Locker getLocker() { ... }
 *        public void setLocker(Locker locker) { ... }
 *        ...
 *    }
 *
 *    &#064;Entity
 *    &#064;AssociationOverrides({
 *        &#064;AssociationOverride(
 *                   name="address",
 *                   joinColumns=&#064;JoinColumn("ADDR_ID")),
 *        &#064;AttributeOverride(
 *                   name="locker",
 *                   joinColumns=&#064;JoinColumn("LCKR_ID"))
 *        })
 *    public PartTimeEmployee { ... }
 * </pre>
 *
 *@see AssociationOverride
 *
 * @since Java Persistence 1.0
 */
@Target({TYPE, METHOD, FIELD})
@Retention(RUNTIME)

public @interface AssociationOverrides {

    /**
     *(Required) The association override mappings that are to be
     * applied to the relationship field or property .
     */
    AssociationOverride[] value();
}
