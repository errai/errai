package org.jboss.workspace.client.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.DOM;
import static com.google.gwt.user.client.DOM.setStyleAttribute;
import com.google.gwt.user.client.Event;
import static com.google.gwt.user.client.Event.addNativePreviewHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import static com.google.gwt.user.client.ui.RootPanel.getBodyElement;
import org.jboss.workspace.client.listeners.CellChangeEvent;
import org.jboss.workspace.client.widgets.format.WSCellFormatter;
import org.jboss.workspace.client.widgets.format.WSCellSimpleTextCell;
import org.jboss.workspace.client.widgets.format.WSCellTitle;

import static java.lang.Double.parseDouble;
import java.util.*;

public class WSGrid extends Composite {
    private FocusPanel fPanel;
    private VerticalPanel panel;
    private WSAbstractGrid titleBar;
    private WSAbstractGrid dataGrid;

    private int cols;

    private Stack<WSCell> selectionList = new Stack<WSCell>();
    private ArrayList<Integer> colSizes = new ArrayList<Integer>();
    private Map<Integer, Boolean> sortedColumns = new HashMap<Integer, Boolean>();

    private WSCell sortedColumnHeader;

    private int startSelX;
    private int startSelY;
    private int fillX;
    private int fillY;

    private boolean currentFocusColumn;

    private boolean _leftGrow = false;
    private boolean _resizeArmed = false;
    private boolean _resizing = false;
    private boolean _rangeSelect = false;

    private boolean resizeOnAttach = false;

    private WSGrid wsGrid = this;
    private PopupPanel resizeLine = new PopupPanel() {
        @Override
        public void onBrowserEvent(Event event) {
            wsGrid.onBrowserEvent(event);
        }
    };

    private List<ChangeHandler> cellChangeHandlers = new LinkedList<ChangeHandler>();

    public WSGrid() {
        this(true);
    }

    private int _startpos = 0;

    public WSGrid(boolean scrollable) {
        panel = new VerticalPanel();
        fPanel = new FocusPanel(panel);

        initWidget(fPanel);

        titleBar = new WSAbstractGrid(false, GridType.TITLEBAR);

        panel.add(titleBar);

        titleBar.setStylePrimaryName("WSGrid-header");
        dataGrid = new WSAbstractGrid(scrollable, GridType.EDITABLE_GRID);

        panel.add(dataGrid);
        panel.setCellVerticalAlignment(dataGrid, HasVerticalAlignment.ALIGN_TOP);

        dataGrid.setStylePrimaryName("WSGrid-datagrid");

        resizeLine.setWidth("5px");
        resizeLine.setHeight("800px");
        resizeLine.setStyleName("WSGrid-resize-line");
        resizeLine.sinkEvents(Event.MOUSEEVENTS);

        dataGrid.getScrollPanel().addScrollHandler(new ScrollHandler() {
            public void onScroll(ScrollEvent event) {
                titleBar.getScrollPanel()
                        .setHorizontalScrollPosition(dataGrid.getScrollPanel().getHorizontalScrollPosition());
            }
        });

        /**
         * This is the handler that is responsible for resizing columns.
         */
        fPanel.addMouseDownHandler(new MouseDownHandler() {
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
         * This handler traces the mouse movement, drawing the vertical resizing indicator.
         */
        fPanel.addMouseMoveHandler(new MouseMoveHandler() {
            public void onMouseMove(MouseMoveEvent event) {
                if (_resizing) {
                    setStyleAttribute(resizeLine.getElement(), "left", (event.getClientX()) + "px");
                }
            }
        });

        /**
         * This is the mouse release handler that resizes a column once the left mouse button is released
         * in a resizing operation.
         */
        fPanel.addMouseUpHandler(new MouseUpHandler() {
            public void onMouseUp(MouseUpEvent event) {
                if (_resizing) {
                    cancelResize();

                    WSCell currentFocus = selectionList.isEmpty() ? null :
                            selectionList.lastElement();

                    int selCol = (_leftGrow ? currentFocus.col - 1 : currentFocus.col);
                    if (selCol == -1) return;

                    int width = colSizes.get(selCol);
                    int offset = event.getClientX();

                    /**
                     * If we didn't move at all, don't calculate a new size.
                     */
                    if (offset - _startpos == 0) return;

                    width -= (_startpos -= event.getClientX());

                    setColumnWidth(selCol, width);
                }

                _rangeSelect = false;
            }
        });


        /**
         * This handler is responsible for all the keyboard input handlers for the grid.
         */
        fPanel.addKeyDownHandler(new KeyDownHandler() {
            public void onKeyDown(KeyDownEvent event) {
                //final WSCell currentFocus = selectionList.isEmpty() ? null : selectionList.lastElement();
                final WSCell currentFocus;
                if (selectionList.isEmpty()) {
                    currentFocus = null;
                }
                else if (selectionList.lastElement().isColSpan()) {
                 //   selectionList.remove(selectionList.lastElement());
                    WSCell c = selectionList.lastElement();

                    currentFocus = dataGrid.tableIndex.get(c.getRow())
                            .get(c.getCol() + c.getColspan());

                    blurAll();
                    selectionList.remove(c);

                    currentFocus.focus();
                }
                else {
                    currentFocus = selectionList.lastElement();
                }

                if (currentFocus == null || currentFocus.edit) {
                    return;
                }

                switch (event.getNativeKeyCode()) {
                    case KeyCodes.KEY_TAB:
                        blurAll();
                        if (event.getNativeEvent().getShiftKey()) {
                            if (currentFocus.getCol() == 0 && currentFocus.getRow() > 0) {
                                dataGrid.tableIndex.get(currentFocus.getRow() - 1).get(cols - 1).focus();
                            }
                            else {
                                dataGrid.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() - 1).focus();
                            }
                        }
                        else {
                            if (currentFocus.getCol() == cols - 1 && currentFocus.getRow() < dataGrid.tableIndex.size()) {
                                dataGrid.tableIndex.get(currentFocus.getRow() + 1).get(0).focus();
                            }
                            else {
                                dataGrid.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() + 1).focus();
                            }
                        }
                        break;
                    case 63232:
                    case KeyCodes.KEY_UP:
                        if (currentFocus.getRow() > 0) {
                            if (!event.getNativeEvent().getShiftKey()) {
                                blurAll();
                            }

                            if (startSelX != -1) {
                                int fill;

                                if (fillX == 0) {
                                    fill = fillX = startSelX - currentFocus.getCol();
                                }
                                else {
                                    fill = fillX;
                                }

                                if (fill < 0) {
                                    fill *= -1;
                                }

                                int currCol = currentFocus.getCol() > startSelX ? startSelX : currentFocus.getCol();
                                if (startSelY < currentFocus.getRow()) {
                                    for (int fillend = currCol + fill + 1; currCol < fillend; currCol++) {
                                        dataGrid.tableIndex.get(currentFocus.getRow()).get(currCol).blur();
                                    }
                                    fillY--;
                                }
                                else {
                                    for (int fillend = currCol + fill + 1; currCol < fillend; currCol++) {
                                        dataGrid.tableIndex.get(currentFocus.getRow() - 1).get(currCol).focus();
                                    }
                                    fillY++;
                                }
                            }

                            dataGrid.tableIndex.get(currentFocus.getRow() - 1).get(currentFocus.getCol())
                                    .focus();
                        }
                        break;
                    case 63235:
                    case KeyCodes.KEY_RIGHT:
                        if (currentFocus.getCol() < cols - 1) {
                            if (!event.getNativeEvent().getShiftKey()) {
                                blurAll();
                            }

                            if (currentFocusColumn) {
                                titleBar.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() + 1).focus();
                            }
                            else {
                                if (startSelY != -1) {
                                    int fill;
                                    if (fillY == 0) {
                                        fill = fillY = currentFocus.getRow() - startSelY;
                                    }
                                    else {
                                        fill = fillY;
                                    }

                                    if (fill < 0) {
                                        fill *= -1;
                                    }

                                    int currRow = currentFocus.getRow() > startSelY ? startSelY : currentFocus.getRow();
                                    if (startSelX > currentFocus.getCol()) {
                                        for (int fillend = currRow + fill + 1; currRow < fillend; currRow++) {
                                            dataGrid.tableIndex.get(currRow).get(currentFocus.getCol()).blur();
                                        }
                                        fillX--;
                                    }
                                    else {
                                        for (int fillend = currRow + fill + 1; currRow < fillend; currRow++) {
                                            dataGrid.tableIndex.get(currRow).get(currentFocus.getCol() + 1).focus();
                                        }
                                        fillX++;
                                    }
                                }

                                dataGrid.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() + 1)
                                        .focus();
                            }

                        }
                        break;
                    case 63233:
                    case KeyCodes.KEY_ENTER:
                    case KeyCodes.KEY_DOWN:
                        if (currentFocus.getRow() < dataGrid.tableIndex.size() - 1) {
                            if (!event.getNativeEvent().getShiftKey()) {
                                blurAll();
                            }

                            if (startSelX != -1) {
                                int fill;

                                if (fillX == 0) {
                                    fill = fillX = currentFocus.getCol() - startSelX;
                                }
                                else {
                                    fill = fillX;
                                }

                                if (fill < 0) {
                                    fill *= -1;
                                }

                                int currCol = currentFocus.getCol() > startSelX ? startSelX : currentFocus.getCol();
                                if (startSelY > currentFocus.getRow()) {
                                    for (int fillend = currCol + fill + 1; currCol < fillend; currCol++) {
                                        dataGrid.tableIndex.get(currentFocus.getRow()).get(currCol).blur();
                                    }
                                    fillY--;
                                }
                                else {
                                    for (int fillend = currCol + fill + 1; currCol < fillend; currCol++) {
                                        dataGrid.tableIndex.get(currentFocus.getRow() + 1).get(currCol).focus();
                                    }
                                    fillY++;
                                }
                            }

                            dataGrid.tableIndex.get(currentFocus.getRow() + 1).get(currentFocus.getCol()).focus();
                        }
                        break;
                    case 63234:
                    case KeyCodes.KEY_LEFT:
                        if (currentFocus.getCol() > 0) {
                            if (!event.getNativeEvent().getShiftKey()) {
                                blurAll();
                            }

                            if (currentFocusColumn) {
                                titleBar.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() - 1).focus();
                            }
                            else {
                                if (startSelY != -1) {
                                    int fill;

                                    if (fillY == 0) {
                                        fill = fillY = startSelY - currentFocus.getRow();
                                    }
                                    else {
                                        fill = fillY;
                                    }

                                    if (fill < 0) {
                                        fill *= -1;
                                    }

                                    int currRow = currentFocus.getRow() > startSelY ? startSelY : currentFocus.getRow();
                                    if (startSelX < currentFocus.getCol()) {
                                        for (int fillend = currRow + fill + 1; currRow < fillend; currRow++) {
                                            dataGrid.tableIndex.get(currRow).get(currentFocus.getCol()).blur();
                                        }
                                        fillX--;
                                    }
                                    else {
                                        for (int fillend = currRow + fill + 1; currRow < fillend; currRow++) {
                                            dataGrid.tableIndex.get(currRow).get(currentFocus.getCol() - 1).focus();
                                        }
                                        fillX++;
                                    }
                                }

                                dataGrid.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() - 1).focus();
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
                        }
                        else {
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

        /**
         * This handler is to prevent certain default browser behaviors when interacting with the grid using
         * keyboard operations.
         */
        addNativePreviewHandler(new Event.NativePreviewHandler() {
            public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
                final WSCell currentFocus = selectionList.isEmpty() ? null :
                        selectionList.lastElement();

                if (currentFocus != null && currentFocus.isEdit()) {
                    return;
                }

                switch (event.getTypeInt()) {
                    case Event.ONKEYDOWN:
                        switch (event.getNativeEvent().getKeyCode()) {
                            case KeyCodes.KEY_TAB:
                                if (currentFocus != null && currentFocus.isEdit()) {
                                    currentFocus.cellFormat.stopedit();
                                }
                            case 63232:
                            case KeyCodes.KEY_UP:
                            case 63235:
                            case KeyCodes.KEY_RIGHT:
                            case 63233:
                            case KeyCodes.KEY_ENTER:
                            case KeyCodes.KEY_DOWN:
                            case 63234:
                            case KeyCodes.KEY_LEFT:
                            case 63272:
                                event.getNativeEvent().preventDefault();
                        }
                }
            }
        });
    }

    public void setColumnHeader(int row, int column, String html) {
        cols = titleBar.ensureRowsAndCols(row + 1, column + 1);
        WSCell wsc = titleBar.getTableIndex().get(row).get(column);
        wsc.setValue(new WSCellTitle(wsc, html));
    }

    public void setCell(int row, int column, String html) {
        int col = dataGrid.ensureRowsAndCols(row + 1, column + 1);
        if (col > cols) cols = col;

        dataGrid.getTableIndex().get(row).get(column).setValue(html);
    }

    public void setCell(int row, int column, WSCellFormatter formatter) {
        int col = dataGrid.ensureRowsAndCols(row + 1, column + 1);
        if (col > cols) cols = col;

        dataGrid.getTableIndex().get(row).get(column).setValue(formatter);
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public int getCols() {
        return cols;
    }

    public void blurAll() {
        Stack<WSCell> stk = new Stack<WSCell>();
        stk.addAll(selectionList);

        for (WSCell cell : stk) {
            cell.blur();
        }
        selectionList.clear();
        fillX = 0;
        fillY = 0;
        startSelX = -1;
        startSelY = -1;
    }


    /**
     * Sets a column width (in pixels). Columns start from 0.
     *
     * @param column - the column
     * @param width  - the width in pixels
     */
    public void setColumnWidth(int column, int width) {
        colSizes.set(column, width);

        if (column >= cols) return;

        if (isAttached()) {
            int currTableWidth = dataGrid.getScrollPanel().getOffsetWidth();

            titleBar.getTable().getColumnFormatter().setWidth(column, width + "px");
            dataGrid.getTable().getColumnFormatter().setWidth(column, width + "px");

            titleBar.tableIndex.get(0).get(column).setWidth(width + "px");

            WSCell c;
            for (int cX = 0; cX < dataGrid.tableIndex.size(); cX++) {
                c = dataGrid.tableIndex.get(cX).get(column);

                /**
                 * Check to see if this cell is merged with other cells.  If so, we need to accumulate
                 * the proper column width.
                 */
                if (c.isColSpan()) {
                    int spanSize = width + 2;
                    for (int span = c.getColspan() - 1; span > 0; span--) {
                        spanSize += colSizes.get(column + span) + 2;
                    }


                    c.setWidth((spanSize + 2) + "px");
                }
                else {

                    c.setWidth(width + "px");
                }
            }

            titleBar.getScrollPanel().setWidth(currTableWidth - 20 + "px");
            dataGrid.getScrollPanel().setWidth(currTableWidth + "px");
        }
        else {
            resizeOnAttach = true;
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

            if (!scrollable) {
                setStyleAttribute(scrollPanel.getElement(), "overflowY", "hidden");
                setStyleAttribute(scrollPanel.getElement(), "overflowX", "hidden");

                scrollPanel.setHeight("18px");
            }

            scrollPanel.add(table);

            tableIndex = new ArrayList<ArrayList<WSCell>>();
            tableIndex.add(new ArrayList<WSCell>());
        }

        public void addCell(int row, String w) {
            int currentColSize = table.getCellCount(row);
            table.addCell(row);
            table.setWidget(row, currentColSize, new WSCell(this, new WSCellSimpleTextCell(w), row, currentColSize));
        }

        public void addRow() {
            table.insertRow(table.getRowCount());
            for (int i = 0; i < cols; i++) {
                addCell(table.getRowCount() - 1, "");
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
            if (scrollPanel != null) scrollPanel.setHeight(height);
        }

        public void setWidth(String width) {
            if (scrollPanel != null) scrollPanel.setWidth(width);
        }

        public int getOffsetHeight() {
            if (scrollPanel != null) return scrollPanel.getOffsetHeight();
            else return table.getOffsetHeight();
        }

        public int getOffsetWidth() {
            if (scrollPanel != null) return scrollPanel.getOffsetWidth();
            else return table.getOffsetWidth();
        }

        @Override
        protected void onAttach() {
            super.onAttach();
        }

        public ArrayList<ArrayList<WSCell>> getTableIndex() {
            return tableIndex;
        }

        public WSCell getCell(int row, int col) {
            return tableIndex.get(row).get(col);
        }

        public void sort(int col, boolean ascending) {
            boolean secondPass = isEmpty(valueAt(col, 0));

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

                    if (i < j) _sort_swap(i, j);
                }
            }
            else {
                while (i < j) {
                    i++;
                    while (_sort_gt(cellAt(i, col), pvtStr)) {
                        i++;
                    }
                    j--;
                    while (_sort_lt(cellAt(j, col), pvtStr)) {
                        j--;
                    }
                    if (i < j) _sort_swap(i, j);
                }
            }

            return j;
        }

        private boolean _sort_gt(WSCell l, WSCell r) {
            if (l == null) return false;

            if (l.numeric && r.numeric) {
                return parseDouble(l.getValue()) > parseDouble(r.getValue());
            }
            else {
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
                    }
                    else if (ll.charAt(i) < rr.charAt(i)) {
                        return false;
                    }
                }

                return isEmpty(ll) && !isEmpty(rr);
            }
        }

        private boolean _sort_lt(WSCell l, WSCell r) {
            if (l == null) return false;

            if (l.numeric && r.numeric) {
                return parseDouble(l.getValue()) < parseDouble(r.getValue());
            }
            else {
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
                    }
                    else if (ll.charAt(i) > rr.charAt(i)) {
                        return false;
                    }
                }

                return isEmpty(ll) && !isEmpty(rr);
            }
        }

        private void _sort_swap(int i, int j) {
            WSCellFormatter t;
            for (int z = 0; z < cols; z++) {
                t = cellFmtAt(i, z);
                setValueAt(i, z, cellFmtAt(j, z));
                setValueAt(j, z, t);
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

    } //end WSAbstractGrid

    private boolean _msie_compatibility = getUserAgent().contains("msie");

    public class WSCell extends Composite {
        protected FlowPanel panel;

        protected WSCellFormatter cellFormat;

        protected boolean numeric;
        protected boolean edit;

        protected int row;
        protected int col;

        protected int rowspan;
        protected int colspan;

        protected WSAbstractGrid grid;

        public WSCell(WSAbstractGrid grid, WSCellFormatter cellFormat, int row, int column) {
            this.grid = grid;
            panel = new FlowPanel();
            panel.setStyleName("WSCell-panel");

            if (grid.tableIndex.size() - 1 < row) {
                while (grid.tableIndex.size() - 1 < row) {
                    grid.tableIndex.add(new ArrayList<WSCell>());
                }
            }
            ArrayList<WSCell> cols = grid.tableIndex.get(row);

            if (cols.size() == 0 || cols.size() - 1 < column) {
                cols.add(this);
            }
            else {
                cols.set(column, this);
            }

            this.cellFormat = cellFormat;

            panel.add(cellFormat.getWidget(wsGrid));

            this.row = row;
            this.col = column;

            initWidget(panel);
            setWidth(colSizes.get(column) + "px");
            setStyleName("WSCell");
            sinkEvents(Event.MOUSEEVENTS | Event.FOCUSEVENTS | Event.ONCLICK | Event.ONDBLCLICK);

            if (_msie_compatibility) {
                if (cellFormat.getTextValue() == null || cellFormat.getTextValue().equals("")) {
                    cellFormat.setValue("&nbsp;");
                }

                disableTextSelectInternal(panel.getElement(), true);
            }
        }

        public WSCell() {
        }

        /**
         * Calling this method will place the cell into edit mode.
         */
        public void edit() {
            String text = cellFormat.getTextValue();

            if (_msie_compatibility && text.equals("&nbsp;")) cellFormat.setValue("");

            edit = cellFormat.edit(this);
        }

        /**
         * Ends a current edit operation.
         */
        public void stopedit() {
            if (edit) {
                edit = false;
                fPanel.setFocus(true);
            }
        }

        /**
         * Blurs the current cell.
         */
        public void blur() {
            if (edit) cellFormat.stopedit();
            removeStyleDependentName("selected");

            if (currentFocusColumn) {
                blurColumn(col);
                currentFocusColumn = false;
            }
            else {
                selectionList.remove(this);
            }
        }

        public void notifyCellUpdate(String newValue) {
            fireAllCellChangeHandlers(this, newValue);
        }

        /**
         * Focuses the curren cell.
         */
        public void focus() {
            WSCell currentFocus = selectionList.isEmpty() ? null :
                    selectionList.lastElement();

            boolean isFocus = currentFocus == this;

            if (selectionList.isEmpty()) {
                startSelX = col;
                startSelY = row;
            }

            selectionList.add(this);

            if (grid.type == GridType.TITLEBAR) {
                currentFocusColumn = true;

                if (!isFocus) {
                    selectColumn(col);
                }
            }
            else {
                int scrollPos = grid.getScrollPanel().getScrollPosition();
                int scrollPosH = grid.getScrollPanel().getHorizontalScrollPosition();

                int cellHeight = getOffsetHeight();
                int cellWidth = getOffsetWidth();

                int bottomCell = DOM.getAbsoluteTop(getElement()) + cellHeight
                        - DOM.getAbsoluteTop(grid.getScrollPanel().getElement()) + scrollPos;
                int rightCell = DOM.getAbsoluteLeft(getElement()) + cellWidth
                        - DOM.getAbsoluteLeft(grid.getScrollPanel().getElement()) + scrollPosH;

                int bottomVisible = grid.getScrollPanel().getOffsetHeight() + scrollPos - 1;
                int topVisible = bottomVisible - grid.getScrollPanel().getOffsetHeight() + 2;

                int rightVisible = grid.getScrollPanel().getOffsetWidth() + scrollPosH - 1;
                int leftVisible = rightVisible - grid.getScrollPanel().getOffsetWidth() + 2;

                if (bottomCell >= (bottomVisible - cellHeight)) {
                    if (scrollPos % cellHeight != 0) {
                        scrollPos += (scrollPos % cellHeight);
                    }

                    grid.getScrollPanel().setScrollPosition(scrollPos + getOffsetHeight());
                }
                else if (bottomCell - cellHeight <= (topVisible)) {
                    if (scrollPos % cellHeight != 0) {
                        scrollPos -= (scrollPos % cellHeight);
                    }
                    grid.getScrollPanel().setScrollPosition(scrollPos - getOffsetHeight());
                }
                else if (rightCell >= (rightVisible)) {
                    if (scrollPosH % cellWidth != 0) {
                        scrollPosH += (scrollPosH % cellWidth);
                    }

                    grid.getScrollPanel().setHorizontalScrollPosition(scrollPosH + getOffsetWidth());
                }
                else if (rightCell - cellWidth <= (leftVisible)) {
                    if (scrollPosH % cellWidth != 0) {
                        scrollPosH -= (scrollPosH % cellWidth);
                    }

                    grid.getScrollPanel().setHorizontalScrollPosition(scrollPosH - getOffsetWidth());
                }
            }

            addStyleDependentName("selected");
        }

        /**
         * Focuses a range between this cell and the currently selected cell.
         */
        public void focusRange() {
            fillX = col - startSelX;
            fillY = row - startSelY;

            int startX = startSelX;
            int startY = startSelY;

            if (fillX < 0) {
                startX = startSelX + fillX;
                fillX *= -1;
            }
            if (fillY < 0) {
                startY = startSelY + fillY;
                fillY *= -1;
            }

            int endX = startX + fillX + 1;
            int endY = startY + fillY + 1;

            for (int x = startX; x < endX; x++) {
                for (int y = startY; y < endY; y++) {
                    dataGrid.tableIndex.get(y).get(x).focus();
                }
            }
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
            }
            else {
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
            panel.add(formatter.getWidget(wsGrid));

            numeric = isNumeric(formatter.getTextValue());
        }

        public String getValue() {
            return cellFormat.getTextValue();
        }

        public void mergeColumns(int cols) {
            final int _row = row;

            for (int i = 1; i < cols; i++) {
                grid.table.removeCell(row, col + 1);

                final WSCell c = this;
                final int _col = col + i;

                grid.tableIndex.get(row).set(col + i, new WSGrid.WSCell() {
                    private WSCell redirect = c;

                    {
                        this.grid = c.grid;
                        this.col = _col;
                        this.row = _row;
                        initWidget(new HTML());
                    }

                    @Override
                    public void blur() {
                        redirect.blur();
                    }

                    @Override
                    public void focus() {
                        redirect.focus();
                        selectionList.remove(selectionList.size()-1);
                        selectionList.add(this);
                    }
                });
            }

            grid.table.getFlexCellFormatter().setColSpan(row, col, cols);

            colspan = cols;
        }

        public boolean isColSpan() {
            return colspan != 0;
        }

        public int getColspan() {
            return colspan;
        }

        @Override
        public void onBrowserEvent(Event event) {
            int leftG = getAbsoluteLeft() + 10;
            int rightG = getAbsoluteLeft() + colSizes.get(col) - 10;

            switch (event.getTypeInt()) {
                case Event.ONMOUSEOVER:
                    if (!_resizing) {
                        addStyleDependentName("hover");
                        if (_rangeSelect) {
                            focusRange();
                        }
                    }

                    break;
                case Event.ONMOUSEOUT:
                    if (!_resizing) removeStyleDependentName("hover");
                    break;

                case Event.ONMOUSEMOVE:
                    if (!_resizing && grid.type == GridType.TITLEBAR) {
                        if (event.getClientX() < leftG) {
                            addStyleDependentName("resize-left");
                            _resizeArmed = true;
                            _leftGrow = true;
                        }
                        else if (event.getClientX() > rightG) {
                            addStyleDependentName("resize-right");
                            _resizeArmed = true;
                            _leftGrow = false;
                        }
                        else {
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
                    }
                    else if (!event.getMetaKey() && !event.getCtrlKey()
                            && !selectionList.isEmpty() && selectionList.lastElement() != this) {
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
                        boolean asc = getColumnSortOrder(col);

                        if (sortedColumnHeader != null) {
                            WSCell old = sortedColumnHeader;
                            sortedColumnHeader = this;
                            old.cellFormat.getWidget(wsGrid);
                        }
                        else {
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
        panel.setHeight(height);
    }

    /**
     * Sets the height of the grid in pixels
     *
     * @param height The height in pixels
     */
    public void setPreciseHeight(int height) {
        int offsetHeight = height - getTitlebarOffsetHeight();
        setHeight(height + "px");
        dataGrid.getScrollPanel().setHeight(offsetHeight + "px");
    }

    /**
     * Set thes the width of the grid.
     *
     * @param width The CSS width string.
     */
    public void setWidth(String width) {
        panel.setWidth(width);
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

    /**
     * Increase or decrease the width by a relative amount.  A positive value will increase the size of the grid,
     * while a negative value will shrink the size.
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
     * If there is a sorted column, this will return an instance of the header cell for that sorted column.
     *
     * @return An instance of WSCell.
     */
    public WSCell getSortedColumnHeader() {
        return sortedColumnHeader;
    }

    /**
     * Returns the sort order of the specified column.  The boolean value <tt>true</tt> if the order is ascending,
     * <tt>false</em> if it's decending or not sorted at all.
     *
     * @param col The column.
     * @return true if ascending
     */
    public boolean getColumnSortOrder(int col) {
        if (sortedColumns.containsKey(col)) {
            return sortedColumns.get(col);
        }
        else {
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

    /**
     * Removes a {@link ChangeHandler} from the grid.
     *
     * @param handler -
     */
    public void removeCellChangeHandler(ChangeHandler handler) {
        cellChangeHandlers.remove(handler);
    }


    private void fireAllCellChangeHandlers(WSCell cell, String newValue) {
        for (ChangeHandler c : cellChangeHandlers) c.onChange(new CellChangeEvent(cell, newValue));
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        int titleHeight = titleBar.getOffsetHeight();

        panel.setCellHeight(titleBar, titleHeight + "px");

        setHeight("450px");
        setWidth("100%");
        dataGrid.setHeight("450px");
        dataGrid.setHeight("450px");

        if (resizeOnAttach) {
            for (int i = 0; i < colSizes.size(); i++) {
                setColumnWidth(i, colSizes.get(i));
            }
        }
    }


    private static final int EDITABLE = 1;
    private static final int TITLEGRID = 1 << 1;

    public enum GridType {
        EDITABLE_GRID(EDITABLE),
        TITLEBAR(TITLEGRID);

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
