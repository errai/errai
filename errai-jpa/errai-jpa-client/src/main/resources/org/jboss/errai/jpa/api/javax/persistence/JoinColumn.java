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

// $Id: JoinColumn.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies a column for joining an entity association or element
 * collection.  If the <code>JoinColumn</code> annotation itself is
 * defaulted, a single join column is assumed and the default values
 * apply.
 *
 * <pre>
 *   Example:
 *
 *   &#064;ManyToOne
 *   &#064;JoinColumn(name="ADDR_ID")
 *   public Address getAddress() { return address; }
 *
 *
 *   Example: unidirectional one-to-many association using a foreign key mapping
 *
 *   // In Customer class
 *   &#064;OneToMany
 *   &#064;JoinColumn(name="CUST_ID") // join column is in table for Order
 *   public Set&#060;Order&#062; getOrders() {return orders;}
 * </pre>
 *
 * @see ManyToOne
 * @see OneToMany
 * @see OneToOne
 * @see JoinTable
 * @see CollectionTable
 *
 * @since Java Persistence 1.0
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface JoinColumn {

    /**
     * (Optional) The name of the foreign key column.
     * The table in which it is found depends upon the
     * context.
     * <ul>
     * <li>If the join is for a OneToOne or ManyToOne
     *  mapping using a foreign key mapping strategy,
     * the foreign key column is in the table of the
     * source entity or embeddable.
     * <li> If the join is for a unidirectional OneToMany mapping
     * using a foreign key mapping strategy, the foreign key is in the
     * table of the target entity.
     * <li> If the join is for a ManyToMany mapping or for a OneToOne
     * or bidirectional ManyToOne/OneToMany mapping using a join
     * table, the foreign key is in a join table.
     * <li> If the join is for an element collection, the foreign
     * key is in a collection table.
     *</ul>
     *
     * <p> Default (only applies if a single join column is used):
     * The concatenation of the following: the name of the
     * referencing relationship property or field of the referencing
     * entity or embeddable class; "_"; the name of the referenced
     * primary key column.
     * If there is no such referencing relationship property or
     * field in the entity, or if the join is for an element collection,
     * the join column name is formed as the
     * concatenation of the following: the name of the entity; "_";
     * the name of the referenced primary key column.
     */
    String name() default "";

    /**
     * (Optional) The name of the column referenced by this foreign
     * key column.
     * <ul>
     * <li> When used with entity relationship mappings other
     * than the cases described here, the referenced column is in the
     * table of the target entity.
     * <li> When used with a unidirectional OneToMany foreign key
     * mapping, the referenced column is in the table of the source
     * entity.
     * <li> When used inside a <code>JoinTable</code> annotation,
     * the referenced key column is in the entity table of the owning
     * entity, or inverse entity if the join is part of the inverse
     * join definition.
     * <li> When used in a <code>CollectionTable</code> mapping, the
     * referenced column is in the table of the entity containing the
     * collection.
     * </ul>
     *
     * <p> Default (only applies if single join column is being
     * used): The same name as the primary key column of the
     * referenced table.
     */
    String referencedColumnName() default "";

    /**
     * (Optional) Whether the property is a unique key.  This is a
     * shortcut for the <code>UniqueConstraint</code> annotation at
     * the table level and is useful for when the unique key
     * constraint is only a single field. It is not necessary to
     * explicitly specify this for a join column that corresponds to a
     * primary key that is part of a foreign key.
     */
    boolean unique() default false;

    /** (Optional) Whether the foreign key column is nullable. */
    boolean nullable() default true;

    /**
     * (Optional) Whether the column is included in
     * SQL INSERT statements generated by the persistence
     * provider.
     */
    boolean insertable() default true;

    /**
     * (Optional) Whether the column is included in
     * SQL UPDATE statements generated by the persistence
     * provider.
     */
    boolean updatable() default true;

    /**
     * (Optional) The SQL fragment that is used when
     * generating the DDL for the column.
     * <p> Defaults to the generated SQL for the column.
     */
    String columnDefinition() default "";

    /**
     * (Optional) The name of the table that contains
     * the column. If a table is not specified, the column
     * is assumed to be in the primary table of the
     * applicable entity.
     *
     * <p> Default:
     * <ul>
     * <li> If the join is for a OneToOne or ManyToOne mapping
     * using a foreign key mapping strategy, the name of the table of
     * the source entity or embeddable.
     * <li> If the join is for a unidirectional OneToMany mapping
     * using a foreign key mapping strategy, the name of the table of
     * the target entity.
     * <li> If the join is for a ManyToMany mapping or
     * for a OneToOne or bidirectional ManyToOne/OneToMany mapping
     * using a join table, the name of the join table.
     * <li> If the join is for an element collection, the name of the collection table.
     * </ul>
     */
    String table() default "";
}
