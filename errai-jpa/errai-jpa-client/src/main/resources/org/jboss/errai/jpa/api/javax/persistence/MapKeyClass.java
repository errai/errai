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

// $Id: MapKeyClass.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies the type of the map key for associations of type
 * <code>java.util.Map</code>.  The map key can be a basic type, an
 * embeddable class, or an entity. If the map is specified using Java
 * generics, the <code>MapKeyClass</code> annotation and associated
 * type need not be specified; otherwise they must be specified.
 *
 * <p> The <code>MapKeyClass</code> annotation is used in conjunction
 * with <code>ElementCollection</code> or one of the collection-valued
 * relationship annotations (<code>OneToMany</code> or <code>ManyToMany</code>).
 * The <code>MapKey</code> annotation is not used when
 * <code>MapKeyClass</code> is specified and vice versa.
 *
 * <pre>
 *
 *    Example 1:
 *
 *    &#064;Entity
 *    public class Item {
 *       &#064;Id int id;
 *       ...
 *       &#064;ElementCollection(targetClass=String.class)
 *       &#064;MapKeyClass(String.class)
 *       Map images;  // map from image name to image filename
 *       ...
 *    }
 *
 *    Example 2:
 *
 *    // MapKeyClass and target type of relationship can be defaulted
 *
 *    &#064;Entity
 *    public class Item {
 *       &#064;Id int id;
 *       ...
 *       &#064;ElementCollection
 *       Map&#060;String, String&#062; images;
 *        ...
 *     }
 *
 *     Example 3:
 *
 *     &#064;Entity
 *     public class Company {
 *        &#064;Id int id;
 *        ...
 *        &#064;OneToMany(targetEntity=com.example.VicePresident.class)
 *        &#064;MapKeyClass(com.example.Division.class)
 *        Map organization;
 *     }
 *
 *     Example 4:
 *
 *     // MapKeyClass and target type of relationship are defaulted
 *
 *     &#064;Entity
 *     public class Company {
 *        &#064;Id int id;
 *        ...
 *        &#064;OneToMany
 *        Map&#060;Division, VicePresident&#062; organization;
 *     }
 *
 * </pre>
 * @see ElementCollection
 * @see OneToMany
 * @see ManyToMany
 * @since Java Persistence 2.0
 */

@Target( { METHOD, FIELD })
@Retention(RUNTIME)
public @interface MapKeyClass {
	/**(Required) The type of the map key.*/
	Class value();
}
