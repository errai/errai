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
 * Type for query parameter objects.
 * @param <T> the type of the parameter
 *
 * @see Query
 * @see TypedQuery
 *
 * @since Java Persistence 2.0
 */
public interface Parameter<T> {

    /**
     * Return the parameter name, or null if the parameter is
     * not a named parameter or no name has been assigned.
     * @return parameter name
     */
    String getName();

    /**
     * Return the parameter position, or null if the parameter
     * is not a positional parameter.
     * @return position of parameter
     */
    Integer getPosition();

    /**
     * Return the Java type of the parameter. Values bound to the
     * parameter must be assignable to this type.
     * This method is required to be supported for criteria queries
     * only.   Applications that use this method for Java
     * Persistence query language queries and native queries will
     * not be portable.
     * @return the Java type of the parameter
     * @throws IllegalStateException if invoked on a parameter
     *         obtained from a Java persistence query language
     *         query or native query when the implementation does
     *         not support this use
     */
     Class<T> getParameterType();
}
