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

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import org.jboss.errai.widgets.client.format.WSCellFormatter;
import org.jboss.errai.widgets.client.format.WSCellSimpleTextCell;
import org.jboss.errai.widgets.client.format.WSCellTitle;
import org.jboss.errai.widgets.client.listeners.CellChangeEvent;

import java.util.*;

import static com.google.gwt.user.client.DOM.setStyleAttribute;
import static com.google.gwt.user.client.ui.RootPanel.getBodyElement;
import static java.lang.Double.parseDouble;

/**
 * A Grid/Table implementation for working with structured data.
 */
@SuppressWarnings({"UnusedDeclaration", "ConstantConditions"})
public class WSGrid extends Composite implements RequiresResize {
    private static final int CELL_HEIGHT_PX = 18;

    private FocusPanel focusPanel;
    private VerticalPanel innerPanel;
    private WSAbstractGrid titleBar;
    private WSAbstractGrid dataGrid;

    private int cols;

    private Stack<WSCell> selectionList = new Stack<WSCell>();
    private ArrayList<Integer> colSizes = new ArrayList<Integer>();

    private Map<Integer, Boolean> sortedColumns = new HashMap<Integer, Boolean>();

    private WSCell sortedColumnHeader;

    private FocusManager fm = new DefaultFocusManager(this);

    private int fillX;
    private int fillY;
    private boolean forwardDirY = true;
    private boolean forwardDirX = true;

    private boolean currentFocusColumn;

    private boolean _leftGrow = false;
    private boolean _resizeArmed = false;
    private boolean _resizing = false;
    private boolean _rangeSelect = false;
    private boolean _mergedCells = false;

    private boolean resizeOnAttach = false;

    private boolean rowSelectionOnly = false;

    private WSGrid wsGrid = this;
    private PopupPanel resizeLine = new PopupPanel() {
        @Override
        public void onBrowserEvent(Event event) {
            wsGrid.onBrowserEvent(event);
        }
    };

    private List<ChangeHandler> cellChangeHandlers = new LinkedList<ChangeHandler>();
    private List<ChangeHandler> afterCellChangeHandlers = new LinkedList<ChangeHandler>();

    public WSGrid() {
        this(true, true);
        this.fm = new DefaultFocusManager(this);
    }

    public WSGrid(FocusManager fm) {
        this(true, true);
        this.fm = fm;
    }

    private int _startpos = 0;

    public WSGrid(boolean scrollable, boolean editable) {
        innerPanel = new VerticalPanel();
        innerPanel.setSpacing(0);

        focusPanel = new FocusPanel(innerPanel);

        initWidget(focusPanel);

        titleBar = new WSAbstractGrid(false, GridType.TITLEBAR);
        innerPanel.add(titleBar);
        innerPanel.setCellVerticalAlignment(titleBar, HasVerticalAlignment.ALIGN_BOTTOM);

        titleBar.setStylePrimaryName("WSGrid-header");
        if (editable)
            dataGrid = new WSAbstractGrid(scrollable, GridType.EDITABLE_GRID);
        else
            dataGrid = new WSAbstractGrid(scrollable, GridType.NONEDITABLE_GRID);

        innerPanel.add(dataGrid);
        innerPanel.setCellVerticalAlignment(dataGrid, HasVerticalAlignment.ALIGN_TOP);

        dataGrid.setStylePrimaryName("WSGrid-datagrid");

        resizeLine.setWidth("5px");
        resizeLine.setHeight("800px");
        resizeLine.setStyleName("WSGrid-resize-line");
        resizeLine.sinkEvents(Event.MOUSEEVENTS);

        dataGrid.getScrollPanel().addScrollHandler(new ScrollHandler() {
            public void onScroll(ScrollEvent event) {
                titleBar.getScrollPanel().setHorizontalScrollPosition(dataGrid.getScrollPanel().getHorizontalScrollPosition());
            }
        });

        /**
         * This is the handler that is responsible for resizing columns.
         */
        focusPanel.addMouseDownHandler(new MouseDownHandler() {
            public void onMouseDown(MouseDownEvent event) {
                if (!selectionList.isEmpty() && selectionList.lastElement().isEdit()) {
                    return;
                }

                if (_resizeArmed) {
                    if (!_resizing) {
                        _resizing = true;

                        disableTextSelection(getBodyElement(), true);

                        _startpos = event.getClientX();

                        resizeLine.show();
                        resizeLine.setPopupPosition(event.getClientX(), 0);
                    }
                }
            }
        });

        /**
         * This handler traces the mouse movement, drawing the vertical resizing
         * indicator.
         */
        focusPanel.addMouseMoveHandler(new MouseMoveHandler() {
            public void onMouseMove(MouseMoveEvent event) {
                if (_resizing) {
                    setStyleAttribute(resizeLine.getElement(), "left", (event.getClientX()) + "px");
                }
            }
        });

        /**
         * This is the mouse release handler that resizes a column once the left
         * mouse button is released in a resizing operation.
         */
        focusPanel.addMouseUpHandler(new MouseUpHandler() {
            public void onMouseUp(MouseUpEvent event) {
                if (_resizing) {
                    cancelResize();

                    WSCell currentFocus = selectionList.isEmpty() ? null : selectionList.lastElement();

                    if (currentFocus == null)
                        return;

                    int selCol = (_leftGrow ? currentFocus.col - 1 : currentFocus.col);
                    if (selCol == -1)
                        return;

                    int width = colSizes.get(selCol);
                    int offset = event.getClientX();

                    /**
                     * If we didn't move at all, don't calculate a new size.
                     */
                    if (offset - _startpos == 0)
                        return;

                    width -= (_startpos -= event.getClientX());

                    setColumnWidth(selCol, width);
                }

                _rangeSelect = false;
            }
        });

        /**
         * This handler is responsible for all the keyboard input handlers for
         * the grid.
         */
        focusPanel.addKeyDownHandler(new KeyDownHandler() {
            public void onKeyDown(KeyDownEvent event) {
                final WSCell currentFocus;
                int offsetX;
                int offsetY;

                /**
                 * Is there currently anything selected?
                 */
                if (selectionList.isEmpty()) {
                    /**
                     * No.
                     */
                    return;
                } else {
                    /**
                     * Set currentFocus to the last element in the
                     * selectionList.
                     */
                    currentFocus = selectionList.lastElement();

                    /**
                     * Yes. Let's check to see if this cell spans. If so, we
                     * will set the offsetX value to the colspan value so
                     * navigation behaves.
                     */
                    offsetX = currentFocus.isColSpan() ? currentFocus.getColspan() : 1;
                    offsetY = currentFocus.isRowSpan() ? currentFocus.getRowspan() : 1;
                }

                /**
                 * If we're currently editing a cell, then we ignore these
                 * operations.
                 */
                if (currentFocus.edit) {
                    return;
                }

                event.preventDefault();

                switch (event.getNativeKeyCode()) {
                    /**
                     * If tab is pressed, we want to advance to the next cell. If we
                     * are at the end of the line, go to the first cell of the next
                     * line. If shift is depressed, then perform the inverse.
                     */
                    case KeyCodes.KEY_TAB:
                        blurAll();
                        if (event.getNativeEvent().getShiftKey()) {
                            if (currentFocus.getCol() == 0 && currentFocus.getRow() > 0) {
                                dataGrid.tableIndex.get(currentFocus.getRow() - offsetX).get(cols - offsetX).focus();
                            } else {
                                dataGrid.tableIndex.get(currentFocus.getRow()).get(
                                        currentFocus.getCol() - (currentFocus.isColSpan() ? currentFocus.getLeftwareColspan() : 1))
                                        .focus();
                            }
                        } else {
                            if (currentFocus.getCol() == cols - offsetX && currentFocus.getRow() < dataGrid.tableIndex.size()) {
                                dataGrid.tableIndex.get(currentFocus.getRow() + offsetX).get(0).focus();
                            } else {
                                dataGrid.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() + offsetX).focus();
                            }
                        }
                        break;

                    /**
                     * If the up key is pressed, we should advance to the cell
                     * directly above the currently selected cell. If we are at the
                     * top of the grid, then nothing should happen.
                     */
                    case 63232:
                    case KeyCodes.KEY_UP:
                        if (currentFocus.getRow() > 0) {
                            if (!event.getNativeEvent().getShiftKey()) {
                                blurAll();
                            }

                            if (fm.isInitialised()) {
                                fm.moveUpwards();
                            } else {
                                dataGrid.tableIndex.get(
                                        currentFocus.getRow() - (currentFocus.isRowSpan() ? currentFocus.getUpwardRowspan() : 1))
                                        .get(currentFocus.getCol()).focus();
                            }
                        }
                        break;

                    /**
                     * If the right key is pressed, we should advance to the cell
                     * directly to the right of the currently selected cell. If we
                     * are at the rightmost part of the grid, then nothing should
                     * happen.
                     */
                    case 63235:
                    case KeyCodes.KEY_RIGHT:
                        if (currentFocus.getCol() < cols - offsetX) {

                            /**
                             * Blur all columns if shift is not being depressed.
                             */
                            if (!event.getNativeEvent().getShiftKey()) {
                                blurAll();
                            }

                            if (currentFocusColumn) {
                                titleBar.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() + offsetX).focus();
                            } else {
                                if (fm.isInitialised()) {
                                    fm.moveRight();
                                } else {

                                    dataGrid.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() + offsetX).focus();
                                }
                            }

                        }
                        break;

                    /**
                     * If either "Enter" or the down key is pressed we should
                     * advance to the cell directly below the current cell. If we
                     * are at the bottom of the grid, depending on the mode of the
                     * grid, either nothing should happen, or a new line should be
                     * added.
                     */
                    case 63233:
                    case KeyCodes.KEY_ENTER:
                    case KeyCodes.KEY_DOWN:
                        if (currentFocus.getRow() < dataGrid.tableIndex.size() - offsetX) {
                            if (!event.getNativeEvent().getShiftKey()) {
                                blurAll();
                            }

                            if (fm.isInitialised()) {
                                fm.moveDownwards();
                            } else {
                                dataGrid.tableIndex.get(currentFocus.getRow() + offsetY).get(currentFocus.getCol()).focus();
                            }
                        }
                        break;

                    /**
                     * If the left key is pressed we should advance to the cell
                     * directly to the left of the currently selected cell. If we
                     * are at the leftmost part of the grid, nothing should happen.
                     */
                    case 63234:
                    case KeyCodes.KEY_LEFT:
                        if (currentFocus.getCol() > 0) {
                            if (!event.getNativeEvent().getShiftKey()) {
                                blurAll();
                            }

                            if (currentFocusColumn) {
                                titleBar.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() - offsetX).focus();
                            } else {
                                if (fm.isInitialised()) {
                                    fm.moveLeft();
                                } else {

                                    int delta = currentFocus.getCol()
                                            - (currentFocus.isColSpan() ? currentFocus.getLeftwareColspan() : 1);

                                    if (delta < 0) {
                                        currentFocus.focus();
                                    } else {
                                        dataGrid.tableIndex.get(currentFocus.getRow()).get(delta).focus();
                                    }
                                }
                            }
                        }
                        break;

                    case KeyCodes.KEY_CTRL:
                    case 16:
                    case 91:
                        break;

                    case KeyCodes.KEY_BACKSPACE:
                    case 63272:
                    case KeyCodes.KEY_DELETE:
                        if (currentFocus.grid.type != GridType.TITLEBAR) {
                            for (WSCell c : selectionList) {
                                c.setValue("");
                            }
                        } else {
                            /**
                             * Wipe the whole column.
                             */
                            for (int i = 0; i < dataGrid.tableIndex.size(); i++) {
                                dataGrid.tableIndex.get(i).get(currentFocus.getCol()).setValue("");
                            }
                        }
                        break;

                    case 32: // spacebar
                        Timer t = new Timer() {
                            public void run() {
                                currentFocus.edit();
                            }
                        };

                        t.schedule(15);
                        break;

                    default:
                        currentFocus.setValue("");
                        currentFocus.edit();
                }
            }
        });

        focusPanel.addMouseOutHandler(new MouseOutHandler() {
            public void onMouseOut(MouseOutEvent mouseOutEvent) {
                _rangeSelect = false;
            }
        });
    }

    public void setScrollable(boolean scrollable) {
        dataGrid.setScrollable(scrollable);
    }

    public void setEditable(boolean editable) {
        if (editable)
            dataGrid.setType(GridType.EDITABLE_GRID);
        else
            dataGrid.setType(GridType.NONEDITABLE_GRID);
    }

    public int getRowCount() {
        return dataGrid.getRowCount();
    }

    public void removeRow(int row) {
        dataGrid.removeRow(row);
    }

    public void setColumnHeader(int row, int column, String html) {
        cols = titleBar.ensureRowsAndCols(row + 1, column + 1);
        WSCell wsc = titleBar.getTableIndex().get(row).get(column);
        wsc.setValue(new WSCellTitle(wsc, html));
    }

    public void setCell(int row, int column, String html) {
        int col = dataGrid.ensureRowsAndCols(row + 1, column + 1);
        if (col > cols)
            cols = col;

        dataGrid.getTableIndex().get(row).get(column).setValue(html);
    }

    public void setCell(int row, int column, WSCellFormatter formatter) {
        int col = dataGrid.ensureRowsAndCols(row + 1, column + 1);
        if (col > cols)
            cols = col;

        dataGrid.getTableIndex().get(row).get(column).setValue(formatter);
    }

    /**
     * Return the total number of columns in the grid.
     *
     * @return -
     */
    public int getCols() {
        return cols;
    }

    /**
     * Blur all currently selected columns.
     */
    public void blurAll() {
        Stack<WSCell> stk = new Stack<WSCell>();
        stk.addAll(selectionList);

        for (WSCell cell : stk) {
            cell.blur();
        }
        selectionList.clear();
        fillX = 0;
        fillY = 0;
        forwardDirX = forwardDirY = true;
        fm.reset();
    }

    public void setRowHeight(int row, int height) {
        dataGrid.setRowHeight(row, height);
    }

    /**
     * Sets a column width (in pixels). Columns start from 0.
     *
     * @param column - the column
     * @param width  - the width in pixels
     */
    public void setColumnWidth(int column, int width) {
        colSizes.set(column, width);

        if (column >= cols)
            return;

        if (isAttached()) {
            titleBar.getTable().getColumnFormatter().setWidth(column, width + "px");
            dataGrid.getTable().getColumnFormatter().setWidth(column, width + "px");

            titleBar.tableIndex.get(0).get(column).setWidth(width + "px");

            WSCell c;
            for (int cX = 0; cX < dataGrid.tableIndex.size(); cX++) {
                if (dataGrid.tableIndex.size() == 0) {
                    // Classic problem in the hosted mode
                    break;
                }

                if (dataGrid.tableIndex.get(cX).size() == 0) {
                    // Classic problem in the hosted mode
                    break;
                }

                c = dataGrid.tableIndex.get(cX).get(column);

                /**
                 * Check to see if this cell is merged with other cells. If so,
                 * we need to accumulate the proper column width.
                 */
                if (c.isColSpan()) {
                    int spanSize = width + 1;
                    for (int span = c.getColspan() - 1; span > 0; span--) {
                        spanSize += colSizes.get(column + span) + 2;
                    }

                    c.setWidth((spanSize + 1) + "px");
                } else {
                    c.setWidth(width + "px");
                }
            }

        } else {
            resizeOnAttach = true;
        }
    }

    private void updateColumnSizes() {
        int col = 0;
        for (Integer size : colSizes) {
            setColumnWidth(col++, size);
        }
    }

    /**
     * Returns an instance of the WSCell based on the row and col specified.
     *
     * @param row - the row
     * @param col - the column
     * @return Instance of WSCell.
     */
    public WSCell getCell(int row, int col) {
        return dataGrid.getCell(row, col);
    }

    /**
     * Highlights a vertical column.
     *
     * @param col - the column
     */
    private void selectColumn(int col) {
        for (ArrayList<WSCell> row : titleBar.getTableIndex()) {
            row.get(col).addStyleDependentName("hcolselect");
        }

        for (ArrayList<WSCell> row : dataGrid.getTableIndex()) {
            row.get(col).addStyleDependentName("colselect");
        }
    }

    /**
     * Blurs a vertical column.
     *
     * @param col - the column
     */
    private void blurColumn(int col) {
        sortedColumns.put(col, false);

        for (ArrayList<WSCell> row : titleBar.getTableIndex()) {
            row.get(col).removeStyleDependentName("hcolselect");
        }

        for (ArrayList<WSCell> row : dataGrid.getTableIndex()) {
            row.get(col).removeStyleDependentName("colselect");
        }
    }

    public void clear() {
        cols = 0;

        this.titleBar.clear();
        this.dataGrid.clear();

        this.selectionList.clear();
        this.colSizes.clear();
        this.sortedColumns.clear();

        this._mergedCells = false;
    }

    /**
     * This is the actual grid implementation.
     */
    public class WSAbstractGrid extends Composite {
        private ScrollPanel scrollPanel;
        private FlexTable table;
        private ArrayList<ArrayList<WSCell>> tableIndex;

        private GridType type;

        public WSAbstractGrid() {
            this(false, GridType.EDITABLE_GRID);
        }

        public WSAbstractGrid(GridType type) {
            this(false, type);
        }

        public WSAbstractGrid(boolean scrollable, GridType type) {
            this.type = type;
            table = new FlexTable();
            table.setStylePrimaryName("WSGrid");
            table.insertRow(0);

            scrollPanel = new ScrollPanel();

            initWidget(scrollPanel);
            scrollPanel.setAlwaysShowScrollBars(scrollable);

            setScrollable(scrollable);

            scrollPanel.add(table);

            tableIndex = new ArrayList<ArrayList<WSCell>>();
            tableIndex.add(new ArrayList<WSCell>());

            setHeight("20px");
        }

        public void setType(GridType type) {
            this.type = type;
        }

        public void setScrollable(boolean scrollable) {
            if (!scrollable) {
                setStyleAttribute(scrollPanel.getElement(), "overflow", "hidden");
                scrollPanel.setHeight("20px");
                table.setHeight("20px");
            } else {
                setStyleAttribute(scrollPanel.getElement(), "overflow", "scroll");
            }
        }

        public void clear() {
            scrollPanel.remove(table);

            table = new FlexTable();
            table.setStylePrimaryName("WSGrid");
            table.insertRow(0);

            scrollPanel.add(table);

            tableIndex.clear();
            tableIndex.add(new ArrayList<WSCell>());
        }

        public void addCell(int row, String w) {
            int currentColSize = table.getCellCount(row);
            table.addCell(row);
            table.setWidget(row, currentColSize, new WSCell(this, new WSCellSimpleTextCell(w), row, currentColSize));
        }

        public int getRowCount() {
            return table.getRowCount();
        }

        public void addRow() {
            table.insertRow(table.getRowCount());
            for (int i = 0; i < cols; i++) {
                addCell(table.getRowCount() - 1, "");
            }
        }

        public void removeRow(int row) {
            table.removeRow(row);
            tableIndex.remove(row);

            // Need to update the rows for all the cells following the deleted
            // row.
            int size = tableIndex.size();
            for (int i = row; i < size; i++) {
                ArrayList<WSCell> currRow = tableIndex.get(i);
                int numCols = currRow.size();
                for (int j = 0; j < numCols; j++)
                    currRow.get(j).row--;
            }
        }

        public int ensureRowsAndCols(int rows, int cols) {
            if (colSizes.size() < cols) {
                for (int i = 0; i < cols; i++) {
                    colSizes.add(125);
                }
            }

            if (table.getRowCount() == 0) {
                addRow();
            }

            while (table.getRowCount() < rows) {
                addRow();
            }

            for (int r = 0; r < table.getRowCount(); r++) {
                if (table.getCellCount(r) < cols) {
                    int growthDelta = cols - table.getCellCount(r);

                    for (int c = 0; c < growthDelta; c++) {
                        table.getColumnFormatter().setWidth(c, colSizes.get(c) + "px");
                        addCell(r, "");
                    }

                    assert table.getCellCount(r) == cols : "New size is wrong: " + table.getCellCount(r);
                }
            }

            return cols == 0 ? 1 : cols;
        }

        public FlexTable getTable() {
            return table;
        }

        public void setHeight(String height) {
            if (scrollPanel != null)
                scrollPanel.setHeight(height);
        }

        public void setWidth(String width) {
            if (scrollPanel != null)
                scrollPanel.setWidth(width);
        }

        public int getOffsetHeight() {
            if (scrollPanel != null)
                return scrollPanel.getOffsetHeight();
            else
                return table.getOffsetHeight();
        }

        public int getOffsetWidth() {
            if (scrollPanel != null)
                return scrollPanel.getOffsetWidth();
            else
                return table.getOffsetWidth();
        }

        @Override
        protected void onAttach() {
            super.onAttach();
        }

        public ArrayList<ArrayList<WSCell>> getTableIndex() {
            return tableIndex;
        }

        public void setRowHeight(int row, int height) {
            if (row > tableIndex.size())
                return;

            ArrayList<WSCell> rowData = tableIndex.get(row);
            String ht = height + "px";
            for (int i = 0; i < cols; i++) {
                rowData.get(i).setHeight(ht);
            }
        }

        public WSCell getCell(int row, int col) {
            return tableIndex.get(row).get(col);
        }

        public void sort(int col, boolean ascending) {
            boolean secondPass = isEmpty(valueAt(0, col));

            _sort(col, ascending, 0, tableIndex.size() - 1);
            if (secondPass) {
                // resort
                _sort(col, ascending, 0, tableIndex.size() - 1);
            }
        }

        private void _sort(int col, boolean ascending, int low, int high) {
            if (low < high) {
                int p = _sort_partition(col, ascending, low, high);
                _sort(col, ascending, low, p);
                _sort(col, ascending, p + 1, high);
            }
        }

        private int _sort_partition(int col, boolean ascending, int low, int high) {
            WSCell pvtStr = cellAt(low, col);

            int i = low - 1;
            int j = high + 1;

            if (ascending) {
                while (i < j) {
                    i++;
                    while (_sort_lt(cellAt(i, col), pvtStr)) {
                        i++;
                    }

                    j--;
                    while (_sort_gt(cellAt(j, col), pvtStr)) {
                        j--;
                    }

                    if (i < j)
                        _sort_swap(i, j);
                }
            } else {
                while (i < j) {
                    i++;
                    while (_sort_gt(cellAt(i, col), pvtStr)) {
                        i++;
                    }
                    j--;
                    while (_sort_lt(cellAt(j, col), pvtStr)) {
                        j--;
                    }
                    if (i < j)
                        _sort_swap(i, j);
                }
            }

            return j;
        }

        private boolean _sort_gt(WSCell l, WSCell r) {
            if (l == null)
                return false;

            if (l.numeric && r.numeric) {
                return parseDouble(l.getValue()) > parseDouble(r.getValue());
            } else {
                String ll = l.getValue();
                String rr = r.getValue();

                // Internet Explorer is very annoying
                if (_msie_compatibility) {
                    ll = ll.equals("&nbsp;") ? "" : ll;
                    rr = rr.equals("&nbsp;") ? "" : rr;
                }

                for (int i = 0; i < ll.length() && i < rr.length(); i++) {
                    if (ll.charAt(i) > rr.charAt(i)) {
                        return true;
                    } else if (ll.charAt(i) < rr.charAt(i)) {
                        return false;
                    }
                }

                return isEmpty(ll) && !isEmpty(rr);
            }
        }

        private boolean _sort_lt(WSCell l, WSCell r) {
            if (l == null)
                return false;

            if (l.numeric && r.numeric) {
                return parseDouble(l.getValue()) < parseDouble(r.getValue());
            } else {
                String ll = l.getValue();
                String rr = r.getValue();

                // Internet Explorer is very annoying
                if (_msie_compatibility) {
                    ll = ll.equals("&nbsp;") ? "" : ll;
                    rr = rr.equals("&nbsp;") ? "" : rr;
                }

                for (int i = 0; i < ll.length() && i < rr.length(); i++) {
                    if (ll.charAt(i) < rr.charAt(i)) {
                        return true;
                    } else if (ll.charAt(i) > rr.charAt(i)) {
                        return false;
                    }
                }

                return isEmpty(ll) && !isEmpty(rr);
            }
        }

        private void _sort_swap(int i, int j) {
            WSCellFormatter t;
            WSCell c1;
            WSCell c2;
            for (int z = 0; z < cols; z++) {
                c1 = cellAt(i, z);
                c2 = cellAt(j, z);
                t = c1.getCellFormat();

                c1.setValue(c2.getCellFormat());
                c2.setValue(t);

                c1.setOriginalRow(j);
                c2.setOriginalRow(i);
            }
        }

        public WSCell cellAt(int row, int col) {
            return tableIndex.size() > row ? tableIndex.get(row).get(col) : null;
        }

        public WSCellFormatter cellFmtAt(int row, int col) {
            return cellAt(row, col).cellFormat;
        }

        public String valueAt(int row, int col) {
            return tableIndex.get(row).get(col).cellFormat.getTextValue();
        }

        public void setValueAt(int row, int col, String html) {
            cellAt(row, col).setValue(html);
        }

        public void setValueAt(int row, int col, WSCellFormatter cellFmt) {
            cellAt(row, col).setValue(cellFmt);
        }

        public ScrollPanel getScrollPanel() {
            return scrollPanel;
        }

    } // end WSAbstractGrid

    private boolean _msie_compatibility = getUserAgent().contains("msie");

    public class WSCell extends Composite {
        protected SimplePanel panel;

        protected WSCellFormatter cellFormat;

        protected boolean numeric;
        protected boolean edit;

        protected int originalRow;
        protected int row;
        protected int col;

        protected int rowspan = 1;
        protected int colspan = 1;

        protected WSAbstractGrid grid;

        public WSCell(WSAbstractGrid grid, WSCellFormatter cellFormat, int row, int column) {
            this.grid = grid;
            panel = new SimplePanel();
            panel.setStyleName("WSCell-panel");

            if (grid.tableIndex.size() - 1 < row) {
                while (grid.tableIndex.size() - 1 < row) {
                    grid.tableIndex.add(new ArrayList<WSCell>());
                }
            }
            ArrayList<WSCell> cols = grid.tableIndex.get(row);

            if (cols.size() == 0 || cols.size() - 1 < column) {
                cols.add(this);
            } else {
                cols.set(column, this);
            }

            this.cellFormat = cellFormat;

            panel.add(cellFormat.getWidget(wsGrid));

            this.row = this.originalRow = row;
            this.col = column;

            initWidget(panel);
            setWidth(colSizes.get(column) + "px");
            setStyleName("WSCell");
            sinkEvents(Event.MOUSEEVENTS | Event.FOCUSEVENTS | Event.ONCLICK | Event.ONDBLCLICK);

            if (_msie_compatibility) {
                if (cellFormat.getTextValue() == null || cellFormat.getTextValue().equals("")) {
                    cellFormat.setValue("&nbsp;");
                }


            }

            disableTextSelectInternal(panel.getElement(), true);
        }

        public WSCell() {
        }

        /**
         * Calling this method will place the cell into edit mode.
         */
        public void edit() {
            String text = cellFormat.getTextValue();

            if (_msie_compatibility && text.equals("&nbsp;"))
                cellFormat.setValue("");

            edit = cellFormat.edit(this);
        }

        /**
         * Ends a current edit operation.
         */
        public void stopedit() {
            if (edit) {
                edit = false;
                if (!_msie_compatibility)
                    focusPanel.setFocus(true);
            }
        }

        public void cancelEdit() {
            cellFormat.cancelEdit();
        }

        /**
         * Blurs the current cell.
         */
        public void blur() {
            if (edit)
                cellFormat.stopedit();
            removeStyleDependentName("selected");

            if (rowSelectionOnly) {
                for (int i = 0; i < cols; i++) {
                    WSCell c = grid.getCell(row, i);

                    if (i == 0) {
                        c.removeStyleDependentName("rowselect-left");
                    } else if (i + 1 == cols) {
                        c.removeStyleDependentName("rowselect-right");
                    } else {
                        c.removeStyleDependentName("rowselect");
                    }
                }
            }

            if (currentFocusColumn) {
                blurColumn(col);
                currentFocusColumn = false;
            } else {
                selectionList.remove(this);
            }
        }

        public void notifyCellUpdate(Object newValue) {
            fireAllCellChangeHandlers(this, newValue);
        }

        public void notifyCellAfterUpdate() {
            fireAllAfterCellChangeHandlers(this);
        }

        /**
         * Focuses the current cell.
         */
        public void focus() {
            WSCell currentFocus = selectionList.isEmpty() ? null : selectionList.lastElement();

            boolean isFocus = currentFocus == this;

            if (selectionList.isEmpty()) {
                fm.setStartCell(this);
            }

            if (!selectionList.contains(this))
                selectionList.add(this);

            if (grid.type == GridType.TITLEBAR) {
                currentFocusColumn = true;

                if (!isFocus) {
                    selectColumn(col);
                }
            } else {
                int scrollPos = grid.getScrollPanel().getScrollPosition();
                int scrollPosH = grid.getScrollPanel().getHorizontalScrollPosition();

                int cellHeight = getOffsetHeight();
                int cellWidth = getOffsetWidth();

                int bottomCell = DOM.getAbsoluteTop(getElement()) + cellHeight
                        - DOM.getAbsoluteTop(grid.getScrollPanel().getElement()) + scrollPos;
                int rightCell = DOM.getAbsoluteLeft(getElement()) + cellWidth
                        - DOM.getAbsoluteLeft(grid.getScrollPanel().getElement()) + scrollPosH;

                int bottomVisible = grid.getScrollPanel().getOffsetHeight() + scrollPos - 19;
                int topVisible = bottomVisible - grid.getScrollPanel().getOffsetHeight() + 2;

                int rightVisible = grid.getScrollPanel().getOffsetWidth() + scrollPosH - 19;
                int leftVisible = rightVisible - grid.getScrollPanel().getOffsetWidth() + 2;

                if (bottomCell >= bottomVisible) {
                    final int startPos = scrollPos;
                    scrollPos += (bottomCell - bottomVisible) + 18;
                    final int endPos = scrollPos;

                    final int multiplier = ((endPos - startPos) / 100);
                    final int threshold = endPos - 50 - (multiplier * 10);
                    final double decelRate = ((double) (endPos - startPos)) / (250 + (multiplier * 100));

                    Timer smoothScroll = new Timer() {
                        int i = startPos;
                        double vel = 5.0 + multiplier;
                        int absoluteVel = (int) Math.round(vel);
                        double decel = decelRate;

                        @Override
                        public void run() {
                            if ((i += absoluteVel) >= endPos) {
                                i = endPos;
                                cancel();
                            }
                            if (i > threshold) {
                                if (vel > 1) {
                                    vel -= decelRate;
                                    absoluteVel = (int) Math.round(vel);
                                    if (absoluteVel < 1)
                                        absoluteVel = 1;
                                }
                            }

                            grid.scrollPanel.setScrollPosition(i);
                        }
                    };
                    smoothScroll.scheduleRepeating(1);

                } else if (bottomCell - cellHeight <= (topVisible)) {
                    final int startPos = scrollPos;
                    scrollPos -= getOffsetHeight();
                    final int endPos = scrollPos;

                    final int multiplier = ((endPos - startPos) / 100);
                    final double decelRate = ((double) (startPos - endPos)) / (250 + (multiplier * 100));
                    final int threshold = endPos + 50 + (multiplier * 10);

                    Timer smoothScroll = new Timer() {
                        int i = startPos;
                        double vel = 5.0 + multiplier;
                        int absoluteVel = (int) Math.round(vel);
                        double decel = decelRate;

                        @Override
                        public void run() {
                            if ((i -= absoluteVel) <= endPos) {
                                i = endPos;
                                cancel();
                            }
                            if (i < threshold) {
                                if (vel > 1) {
                                    vel -= decelRate;
                                    absoluteVel = (int) Math.round(vel);
                                    if (absoluteVel < 1)
                                        absoluteVel = 1;
                                }
                            }

                            grid.scrollPanel.setScrollPosition(i);
                        }
                    };
                    smoothScroll.scheduleRepeating(1);
                } else if (rightCell >= (rightVisible)) {
                    if (scrollPosH % cellWidth != 0) {
                        scrollPosH += (scrollPosH % cellWidth);
                    }

                    grid.getScrollPanel().setHorizontalScrollPosition(scrollPosH + getOffsetWidth());
                } else if (rightCell - cellWidth <= (leftVisible)) {
                    if (scrollPosH % cellWidth != 0) {
                        scrollPosH -= (scrollPosH % cellWidth);
                    }

                    grid.getScrollPanel().setHorizontalScrollPosition(scrollPosH - getOffsetWidth());
                }
            }

            if (grid.type != GridType.TITLEBAR && rowSelectionOnly) {
                for (int i = 0; i < cols; i++) {
                    WSCell c = grid.getCell(row, i);

                    if (!selectionList.contains(c)) {

                        if (i == 0) {
                            c.addStyleDependentName("rowselect-left");
                        } else if (i + 1 == cols) {
                            c.addStyleDependentName("rowselect-right");
                        } else {
                            c.addStyleDependentName("rowselect");
                        }
                    }
                }

                if (col == 0) {
                    addStyleDependentName("rowselect-left");
                } else if (col + 1 == cols) {
                    addStyleDependentName("rowselect-right");
                } else {
                    addStyleDependentName("rowselect");
                }
            } else {

                addStyleDependentName("selected");
            }
        }

        /**
         * Focuses a range between this cell and the currently selected cell.
         */
        public void focusRange() {
            WSCell cell = fm.getStartCell();
            int startSelX = cell.col;
            int startSelY = cell.row;
            fillX = col - startSelX;
            fillY = row - startSelY;

            int startX = startSelX;
            int startY = startSelY;

            if (fillX < 0) {
                startX = startSelX + fillX;
                fillX *= -1;
                forwardDirX = false;
            }
            if (fillY < 0) {
                startY = startSelY + fillY;
                fillY *= -1;
                forwardDirY = false;
            }
            int endX = startX + fillX;
            int endY = startY + fillY;

            int x = startX;
            while (x < endX) {
                if (forwardDirX) {
                    x = x + fm.moveRight();
                } else {
                    x = x + fm.moveLeft();
                }
            }
            ;

            int y = startY;
            while (y < endY) {
                if (forwardDirY) {
                    y = y + fm.moveDownwards();
                } else {
                    y = y + fm.moveUpwards();
                }
            }
            ;
        }

        public int getOriginalRow() {
            return originalRow;
        }

        public void setOriginalRow(int originalRow) {
            this.originalRow = originalRow;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public boolean isEdit() {
            return edit;
        }

        public void setValue(String html) {
            html = html.trim();

            if (_msie_compatibility && html.length() == 0) {
                cellFormat.setValue("&nbsp;");
            } else {
                cellFormat.setValue(html);
                panel.clear();
                panel.add(cellFormat.getWidget(wsGrid));
            }
            numeric = isNumeric(html);

        }

        public void setValue(WSCellFormatter formatter) {
            if (_msie_compatibility && (formatter.getTextValue() == null || formatter.getTextValue().length() == 0)) {
                formatter.setValue("&nbsp;");
            }

            this.cellFormat = formatter;
            panel.clear();

            Widget w = formatter.getWidget(wsGrid);

            panel.add(w);

            numeric = isNumeric(formatter.getTextValue());
        }

        public String getValue() {
            return cellFormat.getTextValue();
        }

        public WSCellFormatter getCellFormat() {
            return cellFormat;
        }

        public void mergeColumns(int cols) {
            if (cols < 2)
                return;

            _mergedCells = true;

            final int _row = row;

            for (int i = 1; i < cols; i++) {
                grid.table.removeCell(row, col + 1);

                final WSCell cell = this;
                final int _col = col + i;

                grid.tableIndex.get(row).set(col + i, new WSGrid.WSCell() {
                    {
                        this.grid = cell.grid;
                        this.col = _col;
                        this.row = _row;
                        initWidget(new HTML());
                    }

                    @Override
                    public void blur() {
                        cell.blur();
                    }

                    @Override
                    public void focus() {
                        cell.focus();
                    }

                    @Override
                    public int getColspan() {
                        return cell.colspan - (col - cell.col);
                    }

                    @Override
                    public int getRowspan() {
                        return cell.rowspan - (row - cell.row);
                    }

                    @Override
                    public int getUpwardRowspan() {
                        return row - cell.row + 1;
                    }

                    @Override
                    public int getLeftwareColspan() {
                        return col - cell.col + 1;
                    }

                    @Override
                    public boolean isColSpan() {
                        return cell.isColSpan();
                    }

                    @Override
                    public boolean isRowSpan() {
                        return cell.isRowSpan();
                    }
                });
            }

            grid.table.getFlexCellFormatter().setColSpan(row, col, cols);
            colspan = cols;

            updateColumnSizes();
        }

        public void mergeRows(int rows) {
            if (rows < 2)
                return;

            _mergedCells = true;

            FlexTable table = grid.table;
            for (int i = 1; i < rows; i++) {
                for (int c = col; c < (col + colspan); c++) {

                    final WSCell cell = this;
                    final int _row = row + i;

                    table.removeCell(_row, col);

                    final int _col = c;

                    grid.tableIndex.get(_row).set(c, new WSCell() {
                        {
                            this.grid = cell.grid;
                            this.col = _col;
                            this.row = _row;
                            initWidget(new HTML());
                        }

                        @Override
                        public void blur() {
                            cell.blur();
                        }

                        @Override
                        public void focus() {
                            cell.focus();
                        }

                        @Override
                        public int getColspan() {
                            return cell.colspan - (col - cell.col);
                        }

                        @Override
                        public int getRowspan() {
                            return cell.rowspan - (row - cell.row);
                        }

                        @Override
                        public int getUpwardRowspan() {
                            return row - cell.row + 1;
                        }

                        @Override
                        public int getLeftwareColspan() {
                            return col - cell.col + 1;
                        }

                        @Override
                        public boolean isColSpan() {
                            return cell.isColSpan();
                        }

                        @Override
                        public boolean isRowSpan() {
                            return cell.isRowSpan();
                        }
                    });

                    grid.table.getFlexCellFormatter().setRowSpan(row, col, rows);
                    rowspan = rows;

                    setHeight(((rows * CELL_HEIGHT_PX)) + "px");

                    updateColumnSizes();
                }
            }
        }

        public boolean isColSpan() {
            return colspan > 1;
        }

        public int getColspan() {
            return colspan;
        }

        public boolean isRowSpan() {
            return rowspan > 1;
        }

        public int getRowspan() {
            return rowspan;
        }

        public int getLeftwareColspan() {
            return 1;
        }

        public int getUpwardRowspan() {
            return 1;
        }

        public void setHeight(String height) {
            super.setHeight(height);
            panel.setHeight(height);
            cellFormat.setHeight(height);
            grid.table.getCellFormatter().setHeight(row, col, height);
        }

        @Override
        public void onBrowserEvent(Event event) {
            int leftG = getAbsoluteLeft() + 10;
            int rightG = getAbsoluteLeft() + colSizes.get(col) - 10;

            switch (event.getTypeInt()) {
                case Event.ONMOUSEOVER:
                    if (!_resizing) {
                        // addStyleDependentName("hover");
                        if (_rangeSelect) {
                            focusRange();
                        }
                    }

                    break;
                case Event.ONMOUSEOUT:
                    // if (!_resizing) removeStyleDependentName("hover");
                    if (grid.type == GridType.TITLEBAR)
                        _resizeArmed = false;
                    break;

                case Event.ONMOUSEMOVE:
                    if (!_resizing && grid.type == GridType.TITLEBAR) {
                        if (event.getClientX() < leftG) {
                            addStyleDependentName("resize-left");
                            _resizeArmed = true;
                            _leftGrow = true;
                        } else if (event.getClientX() > rightG) {
                            addStyleDependentName("resize-right");
                            _resizeArmed = true;
                            _leftGrow = false;
                        } else {
                            removeStyleDependentName("resize-left");
                            removeStyleDependentName("resize-right");
                            _resizeArmed = false;
                        }
                    }

                    break;
                case Event.ONMOUSEDOWN:
                    if (edit) {
                        return;
                    }
                    _rangeSelect = true;

                    if (event.getShiftKey()) {
                        focusRange();
                        break;
                    } else if (!event.getMetaKey() && !event.getCtrlKey() && !selectionList.isEmpty()
                            && selectionList.lastElement() != this) {
                        blurAll();
                    }

                    focus();

                    break;

                case Event.ONMOUSEUP:
                    _rangeSelect = false;
                    break;

                case Event.ONFOCUS:
                    break;

                case Event.ONCLICK:
                    if (grid.type == GridType.TITLEBAR) {
                        if (_mergedCells) {
                            WSModalDialog dialog = new WSModalDialog("Unable to sort");
                            dialog.getCancelButton().setVisible(false);
                            dialog.ask("The table cannot be sorted because it contains merged cells", null);
                            dialog.showModal();
                            return;
                        }

                        boolean asc = getColumnSortOrder(col);

                        if (sortedColumnHeader != null) {
                            WSCell old = sortedColumnHeader;
                            sortedColumnHeader = this;
                            old.cellFormat.getWidget(wsGrid);
                        } else {
                            sortedColumnHeader = this;
                        }
                        cellFormat.getWidget(wsGrid);

                        sortedColumns.put(col, !asc);
                        dataGrid.sort(col, asc);
                    }

                    break;

                case Event.ONDBLCLICK:
                    switch (grid.type) {
                        case EDITABLE_GRID:
                            edit();
                            break;
                        case TITLEBAR:
                            break;
                    }
                    break;
            }
        }
    }

    /**
     * Ends resizing operation.
     */
    private void cancelResize() {
        resizeLine.hide();
        _resizing = _resizeArmed = false;
    }

    /**
     * Returns the offsite high of the title row
     *
     * @return The offset height in pixels
     */
    public int getTitlebarOffsetHeight() {
        return titleBar.getOffsetHeight();
    }

    /**
     * Sets the height of the grid.
     *
     * @param height CSS height string.
     */
    public void setHeight(String height) {
        focusPanel.setHeight(height);
    }

    /**
     * Sets the height of the grid in pixels
     *
     * @param height The height in pixels
     */
    public void setPreciseHeight(int height) {
        int offsetHeight = height - getTitlebarOffsetHeight() - 10;
        setHeight(height + "px");
        if (offsetHeight > 20) {
            dataGrid.getScrollPanel().setHeight((offsetHeight - 20) + "px");
        }
        // ERRAI-72
        else if (offsetHeight < 0) {
            dataGrid.getScrollPanel().setHeight(20 + "px");
        } else {
            dataGrid.getScrollPanel().setHeight(offsetHeight + "px");
        }
    }

    /**
     * Set thes the width of the grid.
     *
     * @param width The CSS width string.
     */
    public void setWidth(String width) {
        focusPanel.setWidth(width);
    }

    /**
     * Sets the width of the grid in pixels.
     *
     * @param width The width in pixels.
     */
    public void setPreciseWidth(int width) {
        setWidth(width + "px");
        titleBar.getScrollPanel().setWidth(width - 20 + "px");
        dataGrid.getScrollPanel().setWidth(width + "px");
    }

    @Override
    public void setPixelSize(int width, int height) {
        setPreciseWidth(width);
        setPreciseHeight(height);
    }

    public void sizeToParent() {
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                if (getParent().isVisible())
                    setPixelSize(getParent().getOffsetWidth(), getParent().getOffsetHeight());
            }
        });

    }

    /**
     * Increase or decrease the width by a relative amount. A positive value
     * will increase the size of the grid, while a negative value will shrink
     * the size.
     *
     * @param amount Size in pixels.
     */
    public void growWidth(int amount) {
        int newWidth = dataGrid.getScrollPanel().getOffsetWidth() + amount;
        setWidth(newWidth + "px");
        titleBar.getScrollPanel().setWidth(newWidth - 20 + "px");
        dataGrid.getScrollPanel().setWidth(newWidth + "px");
    }

    public Map<Integer, Boolean> getSortedColumns() {
        return sortedColumns;
    }

    /**
     * If there is a sorted column, this will return an instance of the header
     * cell for that sorted column.
     *
     * @return An instance of WSCell.
     */
    public WSCell getSortedColumnHeader() {
        return sortedColumnHeader;
    }

    /**
     * Returns the sort order of the specified column. The boolean value
     * <tt>true</tt> if the order is ascending, <tt>false</em> if it's decending or not sorted at all.
     *
     * @param col The column.
     * @return true if ascending
     */
    public boolean getColumnSortOrder(int col) {
        if (sortedColumns.containsKey(col)) {
            return sortedColumns.get(col);
        } else {
            sortedColumns.put(col, true);
            return true;
        }
    }

    /**
     * Registers a {@link ChangeHandler} with the grid.
     *
     * @param handler -
     */
    public void addCellChangeHandler(ChangeHandler handler) {
        cellChangeHandlers.add(handler);
    }

    public void addAfterCellChangeHandler(ChangeHandler handler) {
        afterCellChangeHandlers.add(handler);
    }

    /**
     * Removes a {@link ChangeHandler} from the grid.
     *
     * @param handler -
     */
    public void removeCellChangeHandler(ChangeHandler handler) {
        cellChangeHandlers.remove(handler);
    }

    public void removeAfterCellChangeHandler(ChangeHandler handler) {
        cellChangeHandlers.remove(handler);
    }

    private void fireAllCellChangeHandlers(WSCell cell, Object newValue) {
        for (ChangeHandler c : cellChangeHandlers)
            c.onChange(new CellChangeEvent(cell, newValue));
    }

    private void fireAllAfterCellChangeHandlers(WSCell cell) {
        for (ChangeHandler c : afterCellChangeHandlers)
            c.onChange(new CellChangeEvent(cell, cell.getCellFormat().getValue()));

    }

    public void mergeSelected() {
        WSCell start;

        // TODO Not sure this should use Focus Manager
        int cellX = fm.getStartCell().col;
        int cellY = fm.getStartCell().row;

        if (!forwardDirX) {
            cellX -= fillX;
        }
        if (!forwardDirY) {
            cellY -= fillY;
        }

        start = dataGrid.cellAt(cellY, cellX);

        start.mergeColumns(fillX + 1);
        start.mergeRows(fillY + 1);

        blurAll();
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        int titleHeight = titleBar.getOffsetHeight();
        if (titleHeight > 0) // workaround
            // https://jira.jboss.org/jira/browse/ERRAI-52
            innerPanel.setCellHeight(titleBar, titleHeight + "px");

        if (resizeOnAttach) {
            for (int i = 0; i < colSizes.size(); i++) {
                setColumnWidth(i, colSizes.get(i));
            }
        }
    }

    public void onResize() {
        setPixelSize(getParent().getOffsetWidth(), getParent().getOffsetHeight());
    }

    private static final int NONEDITABLE = 0;
    private static final int EDITABLE = 1;
    private static final int TITLEGRID = 1 << 1;

    public enum GridType {
        NONEDITABLE_GRID(NONEDITABLE), EDITABLE_GRID(EDITABLE), TITLEBAR(TITLEGRID);

        private int options;

        GridType(int options) {
            this.options = options;
        }

        public boolean isEditable() {
            return (EDITABLE & options) != 0;
        }

        public boolean isTitleGrid() {
            return (TITLEGRID & options) != 0;
        }
    }

    public static void disableTextSelection(Element elem, boolean disable) {
        disableTextSelectInternal(elem, disable);
    }

    public Stack<WSCell> getSelectionList() {
        return selectionList;
    }

    public boolean isRowSelectionOnly() {
        return rowSelectionOnly;
    }

    public void setRowSelectionOnly(boolean rowSelectionOnly) {
        this.rowSelectionOnly = rowSelectionOnly;
    }

    private native static void disableTextSelectInternal(Element e, boolean disable)/*-{
																					if (disable) {
																					e.ondrag = function () { return false; };
																					e.onselectstart = function () { return false; };
																					} else {
																					e.ondrag = null;
																					e.onselectstart = null;
																					}
																					}-*/;

    public static native String getUserAgent() /*-{
												return navigator.userAgent.toLowerCase();
												}-*/;

    public static native boolean isNumeric(String input) /*-{
															return (input - 0) == input && input.length > 0;
															}-*/;

	public static native boolean isEmpty(String input) /*-{
														return input == null || input == "";
														}-*/;

}
