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

package javax.persistence.metamodel;

/**
 * Instances of the type <code>MapAttribute</code> represent
 * persistent <code>java.util.Map</code>-valued attributes.
 *
 * @param <X> The type the represented Map belongs to
 * @param <K> The type of the key of the represented Map
 * @param <V> The type of the value of the represented Map
 * @since Java Persistence 2.0
 */
public interface MapAttribute<X, K, V> extends PluralAttribute<X, java.util.Map<K, V>, V> {
	/**
	 * Return the Java type of the map key.
	 *
	 * @return Java key type
	 */
	Class<K> getKeyJavaType();

	/**
	 * Return the type representing the key type of the map.
	 *
	 * @return type representing key type
	 */
	Type<K> getKeyType();
}
