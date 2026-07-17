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

// $Id: PrimaryKeyJoinColumns.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Groups {@link PrimaryKeyJoinColumn} annotations.
 * It is used to map composite foreign keys.
 *
 * <pre>
 *    Example: ValuedCustomer subclass
 *
 *    &#064;Entity
 *    &#064;Table(name="VCUST")
 *    &#064;DiscriminatorValue("VCUST")
 *    &#064;PrimaryKeyJoinColumns({
 *        &#064;PrimaryKeyJoinColumn(name="CUST_ID",
 *            referencedColumnName="ID"),
 *        &#064;PrimaryKeyJoinColumn(name="CUST_TYPE",
 *            referencedColumnName="TYPE")
 *    })
 *    public class ValuedCustomer extends Customer { ... }
 * </pre>
 *
 * @since Java Persistence 1.0
 */
@Target({ TYPE, METHOD, FIELD })
@Retention(RUNTIME)
public @interface PrimaryKeyJoinColumns {
	/**
	 * One or more <code>PrimaryKeyJoinColumn</code> annotations.
	 */
	PrimaryKeyJoinColumn[] value();
}
