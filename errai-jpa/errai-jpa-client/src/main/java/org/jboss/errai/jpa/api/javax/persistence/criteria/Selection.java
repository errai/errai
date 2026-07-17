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

// $Id: Selection.java 20957 2011-06-13 09:58:51Z stliu $

package javax.persistence.criteria;

import javax.persistence.TupleElement;
import java.util.List;

/**
 * The <code>Selection</code> interface defines an item that is to be
 * returned in a query result.
 *
 * @param <X> the type of the selection item
 * @since Java Persistence 2.0
 */
public interface Selection<X> extends TupleElement<X> {
	/**
	 * Assigns an alias to the selection item.
	 * Once assigned, an alias cannot be changed or reassigned.
	 * Returns the same selection item.
	 *
	 * @param name alias
	 *
	 * @return selection item
	 */
	Selection<X> alias(String name);

	/**
	 * Whether the selection item is a compound selection.
	 *
	 * @return boolean indicating whether the selection is a compound
	 *         selection
	 */
	boolean isCompoundSelection();

	/**
	 * Return the selection items composing a compound selection.
	 * Modifications to the list do not affect the query.
	 *
	 * @return list of selection items
	 *
	 * @throws IllegalStateException if selection is not a
	 * compound selection
	 */
	List<Selection<?>> getCompoundSelectionItems();
}

