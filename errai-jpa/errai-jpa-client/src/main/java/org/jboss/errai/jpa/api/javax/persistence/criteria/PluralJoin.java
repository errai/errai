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

package javax.persistence.criteria;

import javax.persistence.metamodel.PluralAttribute;

/**
 * The <code>PluralJoin</code> interface defines functionality
 * that is common to joins to all collection types.  It is
 * not intended to be used directly in query construction.
 *
 * @param <Z> the source type
 * @param <C> the collection type
 * @param <E> the element type of the collection
 * @since Java Persistence 2.0
 */
public interface PluralJoin<Z, C, E> extends Join<Z, E> {

	/**
	 * Return the metamodel representation for the collection-valued
	 * attribute corresponding to the join.
	 *
	 * @return metamodel collection-valued attribute corresponding
	 *         to the target of the join
	 */
	PluralAttribute<? super Z, C, E> getModel();
}
