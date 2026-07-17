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

// $Id: SecondaryTables.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies multiple secondary tables for an entity.
 *
 * <pre>
 *    Example 1: Multiple secondary tables assuming primary key columns are named the same in all tables.
 *
 *    &#064;Entity
 *    &#064;Table(name="EMPLOYEE")
 *    &#064;SecondaryTables({
 *        &#064;SecondaryTable(name="EMP_DETAIL"),
 *        &#064;SecondaryTable(name="EMP_HIST")
 *    })
 *    public class Employee { ... }
 *
 *
 *    Example 2: Multiple secondary tables with differently named primary key columns.
 *
 *    &#064;Entity
 *    &#064;Table(name="EMPLOYEE")
 *    &#064;SecondaryTables({
 *        &#064;SecondaryTable(name="EMP_DETAIL",
 *            pkJoinColumns=&#064;PrimaryKeyJoinColumn(name="EMPL_ID")),
 *        &#064;SecondaryTable(name="EMP_HIST",
 *            pkJoinColumns=&#064;PrimaryKeyJoinColumn(name="EMPLOYEE_ID"))
 *    })
 *    public class Employee { ... }
 * </pre>
 *
 * @since Java Persistence 1.0
 */
@Target(TYPE)
@Retention(RUNTIME)

public @interface SecondaryTables {
	/**
	 * (Required) The secondary tables for an entity.
	 */
	SecondaryTable[] value();
}
