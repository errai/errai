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

// $Id: CollectionTable.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies the table that is used for the mapping of
 * collections of basic or embeddable types.  Applied
 * to the collection-valued field or property.
 *
 * <p>By default, the columns of the collection table that correspond
 * to the embeddable class or basic type are derived from the
 * attributes of the embeddable class or from the basic type according
 * to the default values of the <code>Column</code> annotation. In the case
 * of a basic type, the column name is derived from the name of the
 * collection-valued field or property. In the case of an embeddable
 * class, the column names are derived from the field or property
 * names of the embeddable class.
 * <ul>
 * <li> To override the default properties of the column used for a
 * basic type, the <code>Column</code> annotation is used on the
 * collection-valued attribute in addition to the
 * <code>ElementCollection</code> annotation.
 *
 * <li> To override these defaults for an embeddable class, the
 * <code>AttributeOverride</code> and/or
 * <code>AttributeOverrides</code> annotations can be used in
 * addition to the <code>ElementCollection</code> annotation. If the
 * embeddable class contains references to other entities, the default
 * values for the columns corresponding to those references may be
 * overridden by means of the <code>AssociationOverride</code> and/or
 * <code>AssociationOverrides</code> annotations.
 * </ul>
 *
 * <p> If the <code>CollectionTable</code> annotation is missing, the
 * default values of the <code>CollectionTable</code> annotation
 * elements apply.
 *
 * <pre>
 *    Example:
 *
 *    &#064;Embeddable public class Address {
 *       protected String street;
 *       protected String city;
 *       protected String state;
 *       ...
 *     }
 *
 *    &#064;Entity public class Person {
 *       &#064;Id protected String ssn;
 *       protected String name;
 *       protected Address home;
 *       ...
 *       &#064;ElementCollection  // use default table (PERSON_NICKNAMES)
 *       &#064;Column(name="name", length=50)
 *       protected Set&#060;String&#062; nickNames = new HashSet();
 *       ...
 *    }
 *
 *    &#064;Entity public class WealthyPerson extends Person {
 *       &#064;ElementCollection
 *       &#064;CollectionTable(name="HOMES") // use default join column name
 *       &#064;AttributeOverrides({
 *          &#064;AttributeOverride(name="street",
 *                             column=&#064;Column(name="HOME_STREET")),
 *          &#064;AttributeOverride(name="city",
 *                             column=&#064;Column(name="HOME_CITY")),
 *          &#064;AttributeOverride(name="state",
 *                             column=&#064;Column(name="HOME_STATE"))
 *        })
 *       protected Set&#060;Address&#062; vacationHomes = new HashSet();
 *       ...
 *    }
 * </pre>
 *
 * @see ElementCollection
 * @see AttributeOverride
 * @see AssociationOverride
 * @see Column
 *
 * @since Java Persistence 2.0
 */

@Target( { METHOD, FIELD })
@Retention(RUNTIME)
public @interface CollectionTable {

    /**
     *  (Optional) The name of the collection table.  If not specified,
     *  it defaults to the concatenation of the name of the containing
     *  entity and the name of the collection attribute, separated by
     *  an underscore.
     */
    String name() default "";

    /**
     *  (Optional) The catalog of the table.  If not specified, the
     *  default catalog is used.
     */
    String catalog() default "";

    /**
     * (Optional) The schema of the table.  If not specified, the
     * default schema for the user is used.
     */
    String schema() default "";

    /**
     *  (Optional) The foreign key columns of the collection table
     *  which reference the primary table of the entity.  The default
     *  only applies if a single join column is used.  The default is
     *  the same as for <code>JoinColumn</code> (i.e., the
     *  concatenation of the following: the name of the entity; "_";
     *  the name of the referenced primary key column.) However, if
     *  there is more than one join column, a <code>JoinColumn</code>
     *  annotation must be specified for each join column using the
     *  <code>JoinColumns</code> annotation.  In this case, both the
     *  <code>name</code> and the <code>referencedColumnName</code>
     *  elements must be specified in each such
     *  <code>JoinColumn</code> annotation.
     */
     JoinColumn[] joinColumns() default {};

    /**
     * (Optional) Unique constraints that are to be placed on the
     * table.  These are only used if table generation is in effect.
     */
    UniqueConstraint[] uniqueConstraints() default {};
}
