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

// $Id: Temporal.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation must be specified for persistent fields
 * or properties of type <code>java.util.Date</code> and
 * <code>java.util.Calendar</code>. It may only be specified for fields
 * or properties of these types.
 * <p/>
 * The <code>Temporal</code> annotation may be used in
 * conjunction with the {@link Basic} annotation, the {@link Id}
 * annotation, or the {@link ElementCollection} annotation (when
 * the element collection value is of such a temporal type.
 *
 * <pre>
 *     Example:
 *
 *     &#064;Temporal(DATE)
 *     protected java.util.Date endDate;
 * </pre>
 *
 * @since Java Persistence 1.0
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface Temporal {
	/**
	 * The type used in mapping <code>java.util.Date</code> or <code>java.util.Calendar</code>.
	 */
	TemporalType value();
}
