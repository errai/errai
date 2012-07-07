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

// $Id: Lob.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies that a persistent property or field should be persisted
 * as a large object to a database-supported large object type.
 *
 * <p> Portable applications should use the <code>Lob</code> annotation
 * when mapping to a database Lob type.  The <code>Lob</code>
 * annotation may be used in conjunction with the {@link Basic}
 * annotation or the {@link ElementCollection} annotation when the
 * element collection value is of basic type. A <code>Lob</code> may
 * be either a binary or character type.
 *
 * <p> The <code>Lob</code> type is inferred from the type of the
 * persistent field or property, and except for string and
 * character-based types defaults to Blob.
 * <pre>
 *
 *   Example 1:
 *
 *   &#064;Lob &#064;Basic(fetch=LAZY)
 *   &#064;Column(name="REPORT")
 *   protected String report;
 *
 *   Example 2:
 *
 *   &#064;Lob &#064;Basic(fetch=LAZY)
 *   &#064;Column(name="EMP_PIC", columnDefinition="BLOB NOT NULL")
 *   protected byte[] pic;
 *
 * </pre>
 *
 * @see Basic
 * @see ElementCollection
 *
 * @since Java Persistence 1.0
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Lob {
}
