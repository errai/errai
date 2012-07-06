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

// $Id: Embeddable.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Defines a class whose instances are stored as an intrinsic
 * part of an owning entity and share the identity of the entity.
 * Each of the persistent properties or fields of the embedded
 * object is mapped to the database table for the entity.
 *
 * <p> Note that the {@link Transient} annotation may be used to
 * designate the non-persistent state of an embeddable class.
 *
 * <pre>
 *
 *    Example 1:
 *
 *    &#064;Embeddable public class EmploymentPeriod {
 *       &#064;Temporal(DATE) java.util.Date startDate;
 *       &#064;Temporal(DATE) java.util.Date endDate;
 *      ...
 *    }
 *
 *    Example 2:
 *
 *    &#064;Embeddable public class PhoneNumber {
 *        protected String areaCode;
 *        protected String localNumber;
 *        &#064;ManyToOne PhoneServiceProvider provider;
 *        ...
 *     }
 *
 *    &#064;Entity public class PhoneServiceProvider {
 *        &#064;Id protected String name;
 *         ...
 *     }
 *
 *    Example 3:
 *
 *    &#064;Embeddable public class Address {
 *       protected String street;
 *       protected String city;
 *       protected String state;
 *       &#064;Embedded protected Zipcode zipcode;
 *    }
 *
 *    &#064;Embeddable public class Zipcode {
 *       protected String zip;
 *       protected String plusFour;
 *     }


 * </pre>
 *
 * @since Java Persistence 1.0
 */
@Documented
@Target({TYPE})
@Retention(RUNTIME)
public @interface Embeddable {
}
