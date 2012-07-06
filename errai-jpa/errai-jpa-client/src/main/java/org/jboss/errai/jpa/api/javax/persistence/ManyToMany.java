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

// $Id: ManyToMany.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static javax.persistence.FetchType.LAZY;

/**
 * Defines a many-valued association with many-to-many multiplicity.
 *
 * <p> Every many-to-many association has two sides, the owning side
 * and the non-owning, or inverse, side.  The join table is specified
 * on the owning side. If the association is bidirectional, either
 * side may be designated as the owning side.  If the relationship is
 * bidirectional, the non-owning side must use the <code>mappedBy</code> element of
 * the <code>ManyToMany</code> annotation to specify the relationship field or
 * property of the owning side.
 *
 * <p> The join table for the relationship, if not defaulted, is
 * specified on the owning side.
 *
 * <p> The <code>ManyToMany</code> annotation may be used within an
 * embeddable class contained within an entity class to specify a
 * relationship to a collection of entities. If the relationship is
 * bidirectional and the entity containing the embeddable class is the
 * owner of the relationship, the non-owning side must use the
 * <code>mappedBy</code> element of the <code>ManyToMany</code>
 * annotation to specify the relationship field or property of the
 * embeddable class. The dot (".") notation syntax must be used in the
 * <code>mappedBy</code> element to indicate the relationship
 * attribute within the embedded attribute.  The value of each
 * identifier used with the dot notation is the name of the respective
 * embedded field or property.
 *
 * <pre>
 *
 *    Example 1:
 *
 *    // In Customer class:
 *
 *    &#064;ManyToMany
 *    &#064;JoinTable(name="CUST_PHONES")
 *    public Set&#060;PhoneNumber&#062; getPhones() { return phones; }
 *
 *    // In PhoneNumber class:
 *
 *    &#064;ManyToMany(mappedBy="phones")
 *    public Set&#060;Customer&#062; getCustomers() { return customers; }
 *
 *    Example 2:
 *
 *    // In Customer class:
 *
 *    &#064;ManyToMany(targetEntity=com.acme.PhoneNumber.class)
 *    public Set getPhones() { return phones; }
 *
 *    // In PhoneNumber class:
 *
 *    &#064;ManyToMany(targetEntity=com.acme.Customer.class, mappedBy="phones")
 *    public Set getCustomers() { return customers; }
 *
 *    Example 3:
 *
 *    // In Customer class:
 *
 *    &#064;ManyToMany
 *    &#064;JoinTable(name="CUST_PHONE",
 *        joinColumns=
 *            &#064;JoinColumn(name="CUST_ID", referencedColumnName="ID"),
 *        inverseJoinColumns=
 *            &#064;JoinColumn(name="PHONE_ID", referencedColumnName="ID")
 *        )
 *    public Set&#060;PhoneNumber&#062; getPhones() { return phones; }
 *
 *    // In PhoneNumberClass:
 *
 *    &#064;ManyToMany(mappedBy="phones")
 *    public Set&#060;Customer&#062; getCustomers() { return customers; }
 * </pre>
 *
 * @see JoinTable
 *
 * @since Java Persistence 1.0
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface ManyToMany {

    /**
     * (Optional) The entity class that is the target of the
     * association. Optional only if the collection-valued
     * relationship property is defined using Java generics.  Must be
     * specified otherwise.
     *
     * <p> Defaults to the parameterized type of
     * the collection when defined using generics.
     */
    Class targetEntity() default void.class;

    /**
     * (Optional) The operations that must be cascaded to the target
     * of the association.
     *
     * <p> When the target collection is a {@link java.util.Map
     * java.util.Map}, the <code>cascade</code> element applies to the
     * map value.
     *
     * <p> Defaults to no operations being cascaded.
     */
    CascadeType[] cascade() default {};

    /** (Optional) Whether the association should be lazily loaded or
     * must be eagerly fetched. The EAGER strategy is a requirement on
     * the persistence provider runtime that the associated entities
     * must be eagerly fetched.  The LAZY strategy is a hint to the
     * persistence provider runtime.
     */
    FetchType fetch() default LAZY;

    /**
     * The field that owns the relationship. Required unless
     * the relationship is unidirectional.
     */
    String mappedBy() default "";
}
