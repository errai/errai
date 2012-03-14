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

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static javax.persistence.EnumType.ORDINAL;

/**
 * Specifies the enum type for a map key whose basic type is an enumerated type.
 *
 * The <code>MapKeyEnumerated</code> annotation can be applied to an
 * element collection or relationship of type <code>java.util.Map</code>, in
 * conjunction with the <code>ElementCollection</code>, <code>OneToMany</code>, or
 * <code>ManyToMany</code> annotation.
 * If the enumerated type is not specified or the <code>MapKeyEnumerated</code>
 * annotation is not used, the enumerated type is assumed to be
 * <code>ORDINAL</code>.
 *
 * <pre>
 *   Example:
 *
 *   public enum ProjectStatus {COMPLETE, DELAYED, CANCELLED, IN_PROGRESS}
 *
 *   public enum SalaryRate {JUNIOR, SENIOR, MANAGER, EXECUTIVE}
 *
 *   &#064;Entity public class Employee {
 *       &#064;ManyToMany
 *       public Projects&#060;ProjectStatus, Project&#062; getProjects() {...}
 *
 *       &#064;OneToMany
 *       &#064;MapKeyEnumerated(STRING)
 *       public Map&#060;SalaryRate, Employee&#062; getEmployees() {...}
 *       ...
 *   }
 * </pre>
 *
 * @see ElementCollection
 * @see OneToMany
 * @see ManyToMany
 *
 * @since Java Persistence 2.0
 */
@Target({METHOD, FIELD}) @Retention(RUNTIME)
public @interface MapKeyEnumerated {

    /** (Optional) The type used in mapping a map key enum type. */
    EnumType value() default ORDINAL;
}
