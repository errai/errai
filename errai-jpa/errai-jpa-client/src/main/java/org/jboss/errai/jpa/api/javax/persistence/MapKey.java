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

// $Id: MapKey.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies the map key for associations of type
 * {@link java.util.Map java.util.Map} when the map key is itself the primary
 * key or a persistent field or property of the entity that is
 * the value of the map.
 *
 * <p> If a persistent field or property other than the primary
 * key is used as a map key then it is expected to have a
 * uniqueness constraint associated with it.
 *
 * <p> The {@link MapKeyClass} annotation is not used when
 * <code>MapKey</code> is specified and vice versa.
 *
 * <pre>
 *
 *    Example 1:
 *
 *    &#064;Entity
 *    public class Department {
 *        ...
 *        &#064;OneToMany(mappedBy="department")
 *        &#064;MapKey  // map key is primary key
 *        public Map&#060;Integer, Employee&#062; getEmployees() {... }
 *        ...
 *    }
 *
 *    &#064;Entity
 *    public class Employee {
 *        ...
 *        &#064;Id Integer getEmpId() { ... }
 *        &#064;ManyToOne
 *        &#064;JoinColumn(name="dept_id")
 *        public Department getDepartment() { ... }
 *        ...
 *    }
 *
 *    Example 2:
 *
 *    &#064;Entity
 *        public class Department {
 *        ...
 *        &#064;OneToMany(mappedBy="department")
 *        &#064;MapKey(name="name")
 *        public Map&#060;String, Employee&#062; getEmployees() {... }
 *        ...
 *    }
 *
 *    &#064;Entity
 *        public class Employee {
 *        &#064;Id public Integer getEmpId() { ... }
 *        ...
 *        &#064;ManyToOne
 *        &#064;JoinColumn(name="dept_id")
 *        public Department getDepartment() { ... }
 *        ...
 *    }
 * </pre>
 *
 * @since Java Persistence 1.0
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface MapKey {

    /**
     * (Optional) The name of the persistent field or property of the
     * associated entity that is used as the map key.
     * <p> Default: If the
     * <code>name</code> element is not specified, the primary key of the
     * associated entity is used as the map key. If the
     * primary key is a composite primary key and is mapped
     * as <code>IdClass</code>, an instance of the primary key
     * class is used as the key.
     */
    String name() default "";
}
