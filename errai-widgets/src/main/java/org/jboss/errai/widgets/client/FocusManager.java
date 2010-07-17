/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.widgets.client;

import org.jboss.errai.widgets.client.WSGrid.WSCell;

/**
 * Defines the operations associated with selecting a range of cells within a
 * grid <code>WSGrid</code>. A start cell is first set and the focus range can
 * be thought to extend from the start cell in one of four directions. A "move"
 * is quite distinct to extending the corresponding edge of a selection range;
 * as the consequence can be dependent upon the position of the focus extent
 * relative to the start cell. For example the classic operation would be for a
 * move left to the left of the start cell to set focus; whereas a move left
 * when to the right of the start cell could clear the focus.
 */
public interface FocusManager {

	/**
	 * A move left.
	 * 
	 * @return The number of cells moved (merged cells have their content
	 *         counted individually)
	 */
	public int moveLeft();

	/**
	 * A move right.
	 * 
	 * @return The number of cells moved (merged cells have their content
	 *         counted individually)
	 */
	public int moveRight();

	/**
	 * A move upwards.
	 * 
	 * @return The number of cells moved (merged cells have their content
	 *         counted individually)
	 */
	public int moveUpwards();

	/**
	 * A move downwards.
	 * 
	 * @return The number of cells moved (merged cells have their content
	 *         counted individually)
	 */
	public int moveDownwards();

	/**
	 * The start cell represents the initial selection of a single cell in a
	 * range from which the edges can be thought to extend to encompass a
	 * greater range
	 * 
	 * @param cell
	 */
	public void setStartCell(WSCell cell);

	/**
	 * Return the starting cell.
	 * 
	 * @return
	 */
	public WSCell getStartCell();

	/**
	 * Returns true if the start cell has been set.
	 * 
	 * @return
	 */
	public boolean isInitialised();

	/**
	 * Remove the start cell returning the manager to an uninitialised state
	 */
	public void reset();

}
