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

// $Id: ManyToOne.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static javax.persistence.FetchType.EAGER;

/**
 * Defines a single-valued association to another entity class that
 * has many-to-one multiplicity. It is not normally necessary to
 * specify the target entity explicitly since it can usually be
 * inferred from the type of the object being referenced.  If the
 * relationship is bidirectional, the non-owning
 * <code>OneToMany</code> entity side must used the
 * <code>mappedBy</code> element to specify the relationship field or
 * property of the entity that is the owner of the relationship.
 *
 * <p> The <code>ManyToOne</code> annotation may be used within an
 * embeddable class to specify a relationship from the embeddable
 * class to an entity class. If the relationship is bidirectional, the
 * non-owning <code>OneToMany</code> entity side must use the <code>mappedBy</code>
 * element of the <code>OneToMany</code> annotation to specify the
 * relationship field or property of the embeddable field or property
 * on the owning side of the relationship. The dot (".") notation
 * syntax must be used in the <code>mappedBy</code> element to indicate the
 * relationship attribute within the embedded attribute.  The value of
 * each identifier used with the dot notation is the name of the
 * respective embedded field or property.
 * <pre>
 *
 *     Example 1:
 *
 *     &#064;ManyToOne(optional=false)
 *     &#064;JoinColumn(name="CUST_ID", nullable=false, updatable=false)
 *     public Customer getCustomer() { return customer; }
 *
 *
 *     Example 2:
 *
 *     &#064;Entity
 *        public class Employee {
 *        &#064;Id int id;
 *        &#064;Embedded JobInfo jobInfo;
 *        ...
 *     }
 *
 *     &#064;Embeddable
 *        public class JobInfo {
 *        String jobDescription;
 *        &#064;ManyToOne ProgramManager pm; // Bidirectional
 *     }
 *
 *     &#064;Entity
 *        public class ProgramManager {
 *        &#064;Id int id;
 *        &#064;OneToMany(mappedBy="jobInfo.pm")
 *        Collection&#060;Employee&#062; manages;
 *     }
 *
 * </pre>
 *
 * @since Java Persistence 1.0
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)

public @interface ManyToOne {

    /**
     * (Optional) The entity class that is the target of
     * the association.
     *
     * <p> Defaults to the type of the field or property
     * that stores the association.
     */
    Class targetEntity() default void.class;

    /**
     * (Optional) The operations that must be cascaded to
     * the target of the association.
     *
     * <p> By default no operations are cascaded.
     */
    CascadeType[] cascade() default {};

    /**
     * (Optional) Whether the association should be lazily
     * loaded or must be eagerly fetched. The EAGER
     * strategy is a requirement on the persistence provider runtime that
     * the associated entity must be eagerly fetched. The LAZY
     * strategy is a hint to the persistence provider runtime.
     */
    FetchType fetch() default EAGER;

    /**
     * (Optional) Whether the association is optional. If set
     * to false then a non-null relationship must always exist.
     */
    boolean optional() default true;
}
