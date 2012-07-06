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

// $Id: AttributeOverride.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used to override the mapping of a <code>Basic</code> (whether
 * explicit or default) property or field or <code>Id</code> property or
 * field.
 *
 * <p> May be applied to an entity that extends a mapped superclass or
 * to an embedded field or property to override a basic mapping or id
 * mapping defined by the mapped superclass or embeddable class (or
 * embeddable class of one of its attributes).

 * <p> May be applied to an element collection containing instances of
 * an embeddable class or to a map collection whose key and/or value
 * is an embeddable class. When <code>AttributeOverride</code> is
 * applied to a map, "<code>key.</code>" or "<code>value.</code>" must
 * be used to prefix the name of the attribute that is being
 * overridden in order to specify it as part of the map key or map
 * value.
 *
 * <p> To override mappings at multiple levels of embedding, a dot (".")
 * notation form must be used in the <code>name</code> element to indicate an
 * attribute within an embedded attribute.  The value of each identifier
 * used with the dot notation is the name of the respective embedded
 * field or property.
 *
 * <p> If <code>AttributeOverride</code> is not specified, the column
 * is mapped the same as in the original mapping.
 *
 * <pre>
 *    Example 1:
 *
 *    &#064;MappedSuperclass
 *    public class Employee {
 *        &#064;Id protected Integer id;
 *        &#064;Version protected Integer version;
 *        protected String address;
 *        public Integer getId() { ... }
 *        public void setId(Integer id) { ... }
 *        public String getAddress() { ... }
 *        public void setAddress(String address) { ... }
 *    }
 *
 *    &#064;Entity
 *    &#064;AttributeOverride(name="address", column=&#064;Column(name="ADDR"))
 *    public class PartTimeEmployee extends Employee {
 *        // address field mapping overridden to ADDR
 *        protected Float wage();
 *        public Float getHourlyWage() { ... }
 *        public void setHourlyWage(Float wage) { ... }
 *    }
 *
 *
 *    Example 2:
 *
 *    &#064;Embeddable public class Address {
 *        protected String street;
 *        protected String city;
 *        protected String state;
 *        &#064;Embedded protected Zipcode zipcode;
 *    }
 *
 *    &#064;Embeddable public class Zipcode {
 *        protected String zip;
 *        protected String plusFour;
 *    }
 *
 *    &#064;Entity public class Customer {
 *        &#064;Id protected Integer id;
 *        protected String name;
 *        &#064;AttributeOverrides({
 *            &#064;AttributeOverride(name="state",
 *                               column=&#064;Column(name="ADDR_STATE")),
 *            &#064;AttributeOverride(name="zipcode.zip",
 *                               column=&#064;Column(name="ADDR_ZIP"))
 *        })
 *        &#064;Embedded protected Address address;
 *        ...
 *    }
 *
 *
 *    Example 3:
 *
 *    &#064;Entity public class PropertyRecord {
 *        &#064;EmbeddedId PropertyOwner owner;
 *        &#064;AttributeOverrides({
 *            &#064;AttributeOverride(name="key.street",
 *                               column=&#064;Column(name="STREET_NAME")),
 *            &#064;AttributeOverride(name="value.size",
 *                               column=&#064;Column(name="SQUARE_FEET")),
 *            &#064;AttributeOverride(name="value.tax",
 *                               column=&#064;Column(name="ASSESSMENT"))
 *        })
 *       &#064;ElementCollection
 *       Map&#060;Address, PropertyInfo&#062; parcels;
 *    }
 *
 *   &#064;Embeddable public class PropertyInfo {
 *       Integer parcelNumber;
 *       Integer size;
 *       BigDecimal tax;
 *   }
 *
 * </pre>
 *
 * @see Embedded
 * @see Embeddable
 * @see MappedSuperclass
 * @see AssociationOverride
 *
 * @since Java Persistence 1.0
 */
@Target({TYPE, METHOD, FIELD})
@Retention(RUNTIME)

public @interface AttributeOverride {

    /**
     * (Required) The name of the property whose mapping is being
     * overridden if property-based access is being used, or the
     * name of the field if field-based access is used.
     */
    String name();

    /**
     * (Required) The column that is being mapped to the persistent
     * attribute. The mapping type will remain the same as is
     * defined in the embeddable class or mapped superclass.
     */
    Column column();
}
