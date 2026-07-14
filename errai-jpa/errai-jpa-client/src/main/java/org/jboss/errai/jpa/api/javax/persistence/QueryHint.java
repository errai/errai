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

// $Id: QueryHint.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used to supply a query property or hint to the {@link NamedQuery} or {@link
 * NamedNativeQuery} annotation.
 *
 * <p> Vendor-specific hints that are not recognized by a provider are ignored.
 *
 * @since Java Persistence 1.0
 */
@Target({ })
@Retention(RUNTIME)
public @interface QueryHint {
	/**
	 * Name of the hint.
	 */
	String name();

	/**
	 * Value of the hint.
	 */
	String value();
}
