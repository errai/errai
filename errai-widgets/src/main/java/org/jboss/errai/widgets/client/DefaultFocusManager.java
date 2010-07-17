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
 * A "classic" implementation of FocusManager. A start cell is first selected
 * and corresponding movements from the start cell either extend or diminish the
 * focus range. Left movements relative to the left of the start cell extend the
 * focus range whereas left movements relative to the right of the start cell
 * diminish the focus range. Likewise for the remaining three directions.
 * 
 * @author manstis
 * 
 */
public class DefaultFocusManager implements FocusManager {

	private int activeX;
	private int activeY;
	private int extentLX;
	private int extentRX;
	private int extentTY;
	private int extentBY;
	private int startSelX;
	private int startSelY;
	private WSCell startCell;

	private WSGrid dataGrid;

	public DefaultFocusManager(WSGrid dataGrid) {
		this.dataGrid = dataGrid;
	}

	/**
	 * @see FocusManager#moveLeft()
	 */
	public int moveLeft() {
		int cellCount = 0;
		if (startCell == null) {
			throw new IllegalArgumentException("Start Cell has not been set.");
		}

		activeX--;
		if (activeX < startSelX) {
			cellCount = focusLeftEdge();
		} else if (activeX >= startSelX) {
			cellCount = blurRightEdge();
		}
		return cellCount;
	}

	/**
	 * @see FocusManager#moveRight()
	 */
	public int moveRight() {
		int cellCount = 0;
		if (startCell == null) {
			throw new IllegalArgumentException("Start Cell has not been set.");
		}

		activeX++;
		if (activeX > startSelX) {
			cellCount = focusRightEdge();
		} else if (activeX <= startSelX) {
			cellCount = blurLeftEdge();
		}
		return cellCount;
	}

	/**
	 * @see FocusManager#moveUpwards()
	 */
	public int moveUpwards() {
		int cellCount = 0;
		if (startCell == null) {
			throw new IllegalArgumentException("Start Cell has not been set.");
		}

		activeY--;
		if (activeY < startSelY) {
			cellCount = focusTopEdge();
		} else if (activeY >= startSelY) {
			cellCount = blurBottomEdge();
		}
		return cellCount;
	}

	/**
	 * @see FocusManager#moveDownwards()
	 */
	public int moveDownwards() {
		int cellCount = 0;
		if (startCell == null) {
			throw new IllegalArgumentException("Start Cell has not been set.");
		}

		activeY++;
		if (activeY > startSelY) {
			cellCount = focusBottomEdge();
		} else if (activeY <= startSelY) {
			cellCount = blurTopEdge();
		}
		return cellCount;
	}

	/**
	 * @see FocusManager#setStartCell(WSCell)
	 */
	public void setStartCell(WSCell cell) {
		startCell = cell;
		activeX = startSelX = cell.col;
		activeY = startSelY = cell.row;
		extentLX = cell.getLeftwareColspan() - 1;
		extentRX = cell.getColspan() - 1;
		extentTY = cell.getUpwardRowspan() - 1;
		extentBY = cell.getRowspan() - 1;
	}

	/**
	 * @see FocusManager#getStartCell()
	 */
	public WSCell getStartCell() {
		return startCell;
	}

	/**
	 * @see FocusManager#isInitialised()
	 */
	public boolean isInitialised() {
		return startCell != null;
	}

	/**
	 * @see FocusManager#reset()
	 */
	public void reset() {
		startCell = null;
		activeX = extentLX = extentRX = startSelX = -1;
		activeY = extentTY = extentBY = startSelY = -1;
	}

	private int focusTopEdge() {
		int cellCount = 1;
		extentTY--;
		for (int x = extentLX; x <= extentRX; x++) {
			dataGrid.getCell(startSelY + extentTY, startSelX + x).focus();
		}
		cellCount = cellCount + assertTopEdgeFocus();
		return cellCount;
	}

	private int focusBottomEdge() {
		int cellCount = 1;
		extentBY++;
		for (int x = extentLX; x <= extentRX; x++) {
			dataGrid.getCell(startSelY + extentBY, startSelX + x).focus();
		}
		cellCount = cellCount + assertBottomEdgeFocus();
		return cellCount;
	}

	private int focusLeftEdge() {
		int cellCount = 1;
		extentLX--;
		for (int y = extentTY; y <= extentBY; y++) {
			dataGrid.getCell(startSelY + y, startSelX + extentLX).focus();
		}
		cellCount = cellCount + assertLeftEdgeFocus();
		return cellCount;
	}

	private int focusRightEdge() {
		int cellCount = 1;
		extentRX++;
		for (int y = extentTY; y <= extentBY; y++) {
			dataGrid.getCell(startSelY + y, startSelX + extentRX).focus();
		}
		cellCount = cellCount + assertRightEdgeFocus();
		return cellCount;
	}

	private int blurTopEdge() {
		int cellCount = 1;
		for (int x = extentLX; x <= extentRX; x++) {
			dataGrid.getCell(startSelY + extentTY, startSelX + x).blur();
		}
		extentTY++;
		cellCount = cellCount + assertTopEdgeBlur();
		return cellCount;
	}

	private int blurBottomEdge() {
		int cellCount = 1;
		for (int x = extentLX; x <= extentRX; x++) {
			dataGrid.getCell(startSelY + extentBY, startSelX + x).blur();
		}
		extentBY--;
		cellCount = cellCount + assertBottomEdgeBlur();
		return cellCount;
	}

	private int blurLeftEdge() {
		int cellCount = 1;
		for (int y = extentTY; y <= extentBY; y++) {
			dataGrid.getCell(startSelY + y, startSelX + extentLX).blur();
		}
		extentLX++;
		cellCount = cellCount + assertLeftEdgeBlur();
		return cellCount;
	}

	private int blurRightEdge() {
		int cellCount = 1;
		for (int y = extentTY; y <= extentBY; y++) {
			dataGrid.getCell(startSelY + y, startSelX + extentRX).blur();
		}
		extentRX--;
		cellCount = cellCount + assertRightEdgeBlur();
		return cellCount;
	}

	private int assertTopEdgeFocus() {
		boolean adjustLeft = false;
		boolean adjustRight = false;
		boolean adjustTop = false;
		int cellCount = 0;
		do {
			adjustLeft = false;
			adjustRight = false;
			adjustTop = false;
			for (int x = extentLX; x <= extentRX; x++) {
				WSCell cell = dataGrid.getCell(startSelY + extentTY, startSelX + x);
				if (x == extentLX) {
					if (cell.getLeftwareColspan() > 1) {
						adjustLeft = true;
					}
				}
				if (x == extentRX) {
					if (cell.getColspan() > 1) {
						adjustRight = true;
					}
				}
				if (cell.getUpwardRowspan() > 1) {
					adjustTop = true;
				}
			}
			if (adjustLeft) {
				focusLeftEdge();
			}
			if (adjustRight) {
				focusRightEdge();
			}
			if (adjustTop) {
				cellCount = cellCount + focusTopEdge();
			}
		} while (adjustLeft || adjustRight || adjustTop);
		return cellCount;
	}

	private int assertBottomEdgeFocus() {
		boolean adjustLeft = false;
		boolean adjustRight = false;
		boolean adjustBottom = false;
		int cellCount = 0;
		do {
			adjustLeft = false;
			adjustRight = false;
			adjustBottom = false;
			for (int x = extentLX; x <= extentRX; x++) {
				WSCell cell = dataGrid.getCell(startSelY + extentBY, startSelX + x);
				if (x == extentLX) {
					if (cell.getLeftwareColspan() > 1) {
						adjustLeft = true;
					}
				}
				if (x == extentRX) {
					if (cell.getColspan() > 1) {
						adjustRight = true;
					}
				}
				if (cell.getRowspan() > 1) {
					adjustBottom = true;
				}
			}
			if (adjustLeft) {
				focusLeftEdge();
			}
			if (adjustRight) {
				focusRightEdge();
			}
			if (adjustBottom) {
				cellCount = cellCount + focusBottomEdge();
			}
		} while (adjustLeft || adjustRight || adjustBottom);
		return cellCount;
	}

	private int assertLeftEdgeFocus() {
		boolean adjustLeft = false;
		boolean adjustTop = false;
		boolean adjustBottom = false;
		int cellCount = 0;
		do {
			adjustLeft = false;
			adjustTop = false;
			adjustBottom = false;
			for (int y = extentTY; y <= extentBY; y++) {
				WSCell cell = dataGrid.getCell(startSelY + y, startSelX + extentLX);
				if (y == extentTY) {
					if (cell.getUpwardRowspan() > 1) {
						adjustTop = true;
					}
				}
				if (y == extentBY) {
					if (cell.getRowspan() > 1) {
						adjustBottom = true;
					}
				}
				if (cell.getLeftwareColspan() > 1) {
					adjustLeft = true;
				}
			}
			if (adjustTop) {
				focusTopEdge();
			}
			if (adjustBottom) {
				focusBottomEdge();
			}
			if (adjustLeft) {
				cellCount = cellCount + focusLeftEdge();
			}
		} while (adjustTop || adjustBottom || adjustLeft);
		return cellCount;
	}

	private int assertRightEdgeFocus() {
		boolean adjustRight = false;
		boolean adjustTop = false;
		boolean adjustBottom = false;
		int cellCount = 0;
		do {
			adjustRight = false;
			adjustTop = false;
			adjustBottom = false;
			for (int y = extentTY; y <= extentBY; y++) {
				WSCell cell = dataGrid.getCell(startSelY + y, startSelX + extentRX);
				if (y == extentTY) {
					if (cell.getUpwardRowspan() > 1) {
						adjustTop = true;
					}
				}
				if (y == extentBY) {
					if (cell.getRowspan() > 1) {
						adjustBottom = true;
					}
				}
				if (cell.getColspan() > 1) {
					adjustRight = true;
				}
			}
			if (adjustTop) {
				focusTopEdge();
			}
			if (adjustBottom) {
				focusBottomEdge();
			}
			if (adjustRight) {
				cellCount = cellCount + focusRightEdge();
			}
		} while (adjustTop || adjustBottom || adjustRight);
		return cellCount;
	}

	private int assertTopEdgeBlur() {
		boolean adjustLeft = false;
		boolean adjustRight = false;
		boolean adjustTop = false;
		int cellCount = 0;
		do {
			adjustLeft = false;
			adjustRight = false;
			adjustTop = false;
			for (int x = extentLX; x <= extentRX; x++) {
				WSCell cell = dataGrid.getCell(startSelY + extentTY, startSelX + x);
				if (x == extentLX) {
					if (cell.getLeftwareColspan() > 1) {
						adjustLeft = true;
					}
				}
				if (x == extentRX) {
					if (cell.getColspan() > 1) {
						adjustRight = true;
					}
				}
				if (cell.getUpwardRowspan() > 1) {
					adjustTop = true;
				}
			}
			if (adjustLeft) {
				blurRightEdge();
			}
			if (adjustRight) {
				blurLeftEdge();
			}
			if (adjustTop) {
				cellCount = cellCount + blurTopEdge();
			}
		} while (adjustLeft || adjustRight || adjustTop);
		return cellCount;
	}

	private int assertBottomEdgeBlur() {
		boolean adjustLeft = false;
		boolean adjustRight = false;
		boolean adjustBottom = false;
		int cellCount = 0;
		do {
			adjustLeft = false;
			adjustRight = false;
			adjustBottom = false;
			for (int x = extentLX; x <= extentRX; x++) {
				WSCell cell = dataGrid.getCell(startSelY + extentBY, startSelX + x);
				if (x == extentLX) {
					if (cell.getLeftwareColspan() > 1) {
						adjustLeft = true;
					}
				}
				if (x == extentRX) {
					if (cell.getColspan() > 1) {
						adjustRight = true;
					}
				}
				if (cell.getRowspan() > 1) {
					adjustBottom = true;
				}
			}
			if (adjustLeft) {
				blurRightEdge();
			}
			if (adjustRight) {
				blurLeftEdge();
			}
			if (adjustBottom) {
				cellCount = cellCount + blurBottomEdge();
			}
		} while (adjustLeft || adjustRight || adjustBottom);
		return cellCount;
	}

	private int assertLeftEdgeBlur() {
		boolean adjustRight = false;
		boolean adjustTop = false;
		boolean adjustBottom = false;
		int cellCount = 0;
		do {
			adjustRight = false;
			adjustTop = false;
			adjustBottom = false;
			for (int y = extentTY; y <= extentBY; y++) {
				WSCell cell = dataGrid.getCell(startSelY + y, startSelX + extentLX);
				if (y == extentTY) {
					if (cell.getUpwardRowspan() > 1) {
						adjustTop = true;
					}
				}
				if (y == extentBY) {
					if (cell.getRowspan() > 1) {
						adjustBottom = true;
					}
				}
				if (cell.getLeftwareColspan() > 1) {
					adjustRight = true;
				}
			}
			if (adjustTop) {
				blurBottomEdge();
			}
			if (adjustBottom) {
				blurTopEdge();
			}
			if (adjustRight) {
				cellCount = cellCount + blurLeftEdge();
			}
		} while (adjustTop || adjustBottom || adjustRight);
		return cellCount;
	}

	private int assertRightEdgeBlur() {
		boolean adjustLeft = false;
		boolean adjustTop = false;
		boolean adjustBottom = false;
		int cellCount = 0;
		do {
			adjustLeft = false;
			adjustTop = false;
			adjustBottom = false;
			for (int y = extentTY; y <= extentBY; y++) {
				WSCell cell = dataGrid.getCell(startSelY + y, startSelX + extentRX);
				if (y == extentTY) {
					if (cell.getUpwardRowspan() > 1) {
						adjustTop = true;
					}
				}
				if (y == extentBY) {
					if (cell.getRowspan() > 1) {
						adjustBottom = true;
					}
				}
				if (cell.getColspan() > 1) {
					adjustLeft = true;
				}
			}
			if (adjustTop) {
				blurBottomEdge();
			}
			if (adjustBottom) {
				blurTopEdge();
			}
			if (adjustLeft) {
				cellCount = cellCount + blurRightEdge();
			}
		} while (adjustTop || adjustBottom || adjustLeft);
		return cellCount;
	}

}
