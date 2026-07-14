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

// $Id: DiscriminatorValue.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies the value of the discriminator column for
 * entities of the given type.
 *
 * <p> The <code>DiscriminatorValue</code>
 * annotation can only be specified on a concrete entity
 * class.
 *
 * <p> If the <code>DiscriminatorValue</code> annotation is not
 * specified and a discriminator column is used, a provider-specific
 * function will be used to generate a value representing the
 * entity type.  If the {@link DiscriminatorType} is <code>
 * STRING</code>, the discriminator value
 * default is the entity name.
 *
 * <p> The inheritance strategy and the discriminator column
 * are only specified in the root of an entity class hierarchy
 * or subhierarchy in which a different inheritance strategy is
 * applied. The discriminator value, if not defaulted, should be
 * specified for each entity class in the hierarchy.
 *
 * <pre>
 *
 *    Example:
 *
 *    &#064;Entity
 *    &#064;Table(name="CUST")
 *    &#064;Inheritance(strategy=SINGLE_TABLE)
 *    &#064;DiscriminatorColumn(name="DISC", discriminatorType=STRING, length=20)
 *    &#064;DiscriminatorValue("CUSTOMER")
 *    public class Customer { ... }
 *
 *    &#064;Entity
 *    &#064;DiscriminatorValue("VCUSTOMER")
 *    public class ValuedCustomer extends Customer { ... }
 * </pre>
 *
 * @see DiscriminatorColumn
 *
 * @since Java Persistence 1.0
 */
@Target({TYPE})
@Retention(RUNTIME)

public @interface DiscriminatorValue {

    /**
     * (Optional) The value that indicates that the
     * row is an entity of the annotated entity type.
     *
     * <p> If the <code>DiscriminatorValue</code> annotation is not
     * specified and a discriminator column is used, a
     * provider-specific function will be used to generate a value
     * representing the entity type.  If the <code>DiscriminatorType</code> is
     * <code>STRING</code>, the discriminator value default is the
     * entity name.
     */
    String value();
}
