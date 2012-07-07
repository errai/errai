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

// $Id: $

package javax.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Designates a <code>ManyToOne</code> or
 * <code>OneToOne</code> relationship attribute that provides the
 * mapping for an {@link EmbeddedId} primary key, an attribute within
 * an <code>EmbeddedId</code> primary key, or a simple primary key of
 * the parent entity. The <code>value</code> element specifies the
 * attribute within a composite key to which the relationship
 * attribute corresponds. If the entity's primary key is of the same
 * Java type as the primary key of the entity referenced by the
 * relationship, the value attribute is not specified.
 *
 * <pre>
 *    Example:
 *
 *    // parent entity has simple primary key
 *
 *    &#064;Entity
 *    public class Employee {
 *       &#064;Id long empId;
 *       String name;
 *       ...
 *    }
 *
 *    // dependent entity uses EmbeddedId for composite key
 *
 *    &#064;Embeddable
 *    public class DependentId {
 *       String name;
 *       long empid;   // corresponds to primary key type of Employee
 *    }
 *
 *    &#064;Entity
 *    public class Dependent {
 *       &#064;EmbeddedId DependentId id;
 *        ...
 *       &#064;MapsId("empid")  //  maps the empid attribute of embedded id
 *       &#064;ManyToOne Employee emp;
 *    }
 * </pre>
 *
 * @since Java Persistence 2.0
 */
@Target( { METHOD, FIELD })
@Retention(RUNTIME)
public @interface MapsId {

    /**
     * (Optional) The name of the attribute within the composite key
     * to which the relationship attribute corresponds.  If not
     * supplied, the relationship maps the entitys primary
     * key.
     */
   String value() default "";
}
