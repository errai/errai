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

// $Id: Metamodel.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence.metamodel;

import java.util.Set;

/**
 * Provides access to the metamodel of persistent
 * entities in the persistence unit.
 *
 * @since Java Persistence 2.0
 */
public interface Metamodel {
	/**
	 * Return the metamodel entity type representing the entity.
	 *
	 * @param cls the type of the represented entity
	 *
	 * @return the metamodel entity type
	 *
	 * @throws IllegalArgumentException if not an entity
	 */
	<X> EntityType<X> entity(Class<X> cls);

	/**
	 * Return the metamodel managed type representing the
	 * entity, mapped superclass, or embeddable class.
	 *
	 * @param cls the type of the represented managed class
	 *
	 * @return the metamodel managed type
	 *
	 * @throws IllegalArgumentException if not a managed class
	 */
	<X> ManagedType<X> managedType(Class<X> cls);

	/**
	 * Return the metamodel embeddable type representing the
	 * embeddable class.
	 *
	 * @param cls the type of the represented embeddable class
	 *
	 * @return the metamodel embeddable type
	 *
	 * @throws IllegalArgumentException if not an embeddable class
	 */
	<X> EmbeddableType<X> embeddable(Class<X> cls);

	/**
	 * Return the metamodel managed types.
	 *
	 * @return the metamodel managed types
	 */
	Set<ManagedType<?>> getManagedTypes();

	/**
	 * Return the metamodel entity types.
	 *
	 * @return the metamodel entity types
	 */
	Set<EntityType<?>> getEntities();

	/**
	 * Return the metamodel embeddable types.  Returns empty set
	 * if there are no embeddable types.
	 *
	 * @return the metamodel embeddable types
	 */
	Set<EmbeddableType<?>> getEmbeddables();
}
