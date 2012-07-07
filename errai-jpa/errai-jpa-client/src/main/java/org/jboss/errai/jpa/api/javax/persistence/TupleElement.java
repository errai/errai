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

// $Id: $

package javax.persistence;

/**
 * The <code>TupleElement</code> interface defines an element that is returned in
 * a query result tuple.
 *
 * @param <X> the type of the element
 * @see Tuple
 * @since Java Persistence 2.0
 */
public interface TupleElement<X> {
	/**
	 * Return the Java type of the tuple element.
	 *
	 * @return the Java type of the tuple element
	 */
	Class<? extends X> getJavaType();

	/**
	 * Return the alias assigned to the tuple element or null,
	 * if no alias has been assigned.
	 *
	 * @return alias
	 */
	String getAlias();
}
