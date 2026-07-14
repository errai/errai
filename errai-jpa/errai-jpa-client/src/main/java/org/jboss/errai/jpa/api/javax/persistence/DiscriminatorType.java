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

// $Id: DiscriminatorType.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

/**
 * Defines supported types of the discriminator column.
 *
 * @since Java Persistence 1.0
 */
public enum DiscriminatorType {

    /**
     * String as the discriminator type.
     */
    STRING,

    /**
     * Single character as the discriminator type.
     */
    CHAR,

    /**
     * Integer as the discriminator type.
     */
    INTEGER
}
