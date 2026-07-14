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

// $Id: OneToOne.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static javax.persistence.FetchType.EAGER;

/**
 * Defines a single-valued association to another entity that has
 * one-to-one multiplicity. It is not normally necessary to specify
 * the associated target entity explicitly since it can usually be
 * inferred from the type of the object being referenced.  If the relationship is
 * bidirectional, the non-owning side must use the <code>mappedBy</code> element of
 * the <code>OneToOne</code> annotation to specify the relationship field or
 * property of the owning side.
 *
 * <p> The <code>OneToOne</code> annotation may be used within an
 * embeddable class to specify a relationship from the embeddable
 * class to an entity class. If the relationship is bidirectional and
 * the entity containing the embeddable class is on the owning side of
 * the relationship, the non-owning side must use the
 * <code>mappedBy</code> element of the <code>OneToOne</code>
 * annotation to specify the relationship field or property of the
 * embeddable class. The dot (".") notation syntax must be used in the
 * <code>mappedBy</code> element to indicate the relationship attribute within the
 * embedded attribute.  The value of each identifier used with the dot
 * notation is the name of the respective embedded field or property.
 *
 * <pre>
 *    Example 1: One-to-one association that maps a foreign key column
 *
 *    // On Customer class:
 *
 *    &#064;OneToOne(optional=false)
 *    &#064;JoinColumn(
 *    	name="CUSTREC_ID", unique=true, nullable=false, updatable=false)
 *    public CustomerRecord getCustomerRecord() { return customerRecord; }
 *
 *    // On CustomerRecord class:
 *
 *    &#064;OneToOne(optional=false, mappedBy="customerRecord")
 *    public Customer getCustomer() { return customer; }
 *
 *
 *    Example 2: One-to-one association that assumes both the source and target share the same primary key values.
 *
 *    // On Employee class:
 *
 *    &#064;Entity
 *    public class Employee {
 *    	&#064;Id Integer id;
 *
 *    	&#064;OneToOne &#064;MapsId
 *    	EmployeeInfo info;
 *    	...
 *    }
 *
 *    // On EmployeeInfo class:
 *
 *    &#064;Entity
 *    public class EmployeeInfo {
 *    	&#064;Id Integer id;
 *    	...
 *    }
 *
 *
 *    Example 3: One-to-one association from an embeddable class to another entity.
 *
 *    &#064;Entity
 *    public class Employee {
 *       &#064;Id int id;
 *       &#064;Embedded LocationDetails location;
 *       ...
 *    }
 *
 *    &#064;Embeddable
 *    public class LocationDetails {
 *       int officeNumber;
 *       &#064;OneToOne ParkingSpot parkingSpot;
 *       ...
 *    }
 *
 *    &#064;Entity
 *    public class ParkingSpot {
 *       &#064;Id int id;
 *       String garage;
 *       &#064;OneToOne(mappedBy="location.parkingSpot") Employee assignedTo;
 *        ...
 *    }
 *
 * </pre>
 *
 * @since Java Persistence 1.0
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)

public @interface OneToOne {

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

    /** (Optional) The field that owns the relationship. This
      * element is only specified on the inverse (non-owning)
      * side of the association.
     */
    String mappedBy() default "";


    /**
     * (Optional) Whether to apply the remove operation to entities that have
     * been removed from the relationship and to cascade the remove operation to
     * those entities.
     * @since Java Persistence 2.0
     */
    boolean orphanRemoval() default false;
}
