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

// $Id: CollectionJoin.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence.criteria;

import java.util.Collection;
import javax.persistence.metamodel.CollectionAttribute;

/**
 * The <code>CollectionJoin</code> interface is the type of the result of
 * joining to a collection over an association or element
 * collection that has been specified as a <code>java.util.Collection</code>.
 *
 * @param <Z> the source type of the join
 * @param <E> the element type of the target <code>Collection</code>
 * @since Java Persistence 2.0
 */
public interface CollectionJoin<Z, E>
		extends PluralJoin<Z, Collection<E>, E> {

	/**
	 * Return the metamodel representation for the collection
	 * attribute.
	 *
	 * @return metamodel type representing the <code>Collection</code> that is
	 *         the target of the join
	 */
	CollectionAttribute<? super Z, E> getModel();
}
