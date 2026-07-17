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

// $Id: PersistenceUtil.java 17036 2009-07-08 09:09:38Z epbernard $

package javax.persistence;

/**
 * Utility interface between the application and the persistence
 * provider managing the persistence unit.
 * <p/>
 * <p>The methods of this interface should only be invoked on entity
 * instances obtained from or managed by entity managers for this
 * persistence unit or on new entity instances.
 *
 * @since Java Persistence 2.0
 */
public interface PersistenceUnitUtil extends PersistenceUtil {
	/**
	 * Determine the load state of a given persistent attribute
	 * of an entity belonging to the persistence unit.
	 *
	 * @param entity entity instance containing the attribute
	 * @param attributeName name of attribute whose load state is
	 * to be determined
	 *
	 * @return false if entity's state has not been loaded or if
	 *         the attribute state has not been loaded, else true
	 */
	public boolean isLoaded(Object entity, String attributeName);

	/**
	 * Determine the load state of an entity belonging to the
	 * persistence unit.  This method can be used to determine the
	 * load state of an entity passed as a reference.  An entity is
	 * considered loaded if all attributes for which
	 * <code>FetchType.EAGER</code> has been specified have been
	 * loaded.
	 * <p> The <code>isLoaded(Object, String)</code> method
	 * should be used to determine the load state of an attribute.
	 * Not doing so might lead to unintended loading of state.
	 *
	 * @param entity entity instance whose load state is to be determined
	 *
	 * @return false if the entity has not been loaded, else true
	 */
	public boolean isLoaded(Object entity);

	/**
	 * Return the id of the entity.
	 * A generated id is not guaranteed to be available until after
	 * the database insert has occurred.
	 * Returns null if the entity does not yet have an id.
	 *
	 * @param entity entity instance
	 *
	 * @return id of the entity
	 *
	 * @throws IllegalArgumentException if the object is found not
	 * to be an entity
	 */
	public Object getIdentifier(Object entity);
}
