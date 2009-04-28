package org.jboss.workspace.client.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.DOM;
import static com.google.gwt.user.client.DOM.setStyleAttribute;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;
import static com.google.gwt.user.client.ui.RootPanel.getBodyElement;

import static java.lang.Double.parseDouble;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WSGrid extends Composite {
    private FocusPanel fPanel;
    private VerticalPanel panel;
    private WSAbstractGrid titleBar;
    private WSAbstractGrid dataGrid;

    private ArrayList<Integer> columnWidths;

    private int cols;

    private WSCell currentFocus;
    private boolean currentFocusRowColSpan;
    private boolean _leftGrow = false;

    private boolean _resizeArmed = false;
    private boolean _resizing = false;

    private WSGrid wsGrid = this;
    private PopupPanel resizeLine = new PopupPanel() {
        @Override
        public void onBrowserEvent(Event event) {
            wsGrid.onBrowserEvent(event);
        }
    };

    private ArrayList<Integer> colSizes = new ArrayList<Integer>();
    private Map<Integer, Boolean> sortedColumns = new HashMap<Integer, Boolean>();

    public WSGrid() {
        this(true);
    }

    //  private int _fpanel_offset = -1;
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

        columnWidths = new ArrayList<Integer>();

        resizeLine.setWidth("5px");
        resizeLine.setHeight("800px");
        resizeLine.setStyleName("WSGrid-resize-line");
        resizeLine.sinkEvents(Event.MOUSEEVENTS);


        dataGrid.getScrollPanel().addScrollHandler(new ScrollHandler() {
            public void onScroll(ScrollEvent event) {
                titleBar.getScrollPanel().setHorizontalScrollPosition(dataGrid.getScrollPanel().getHorizontalScrollPosition());
            }
        });

        fPanel.addMouseDownHandler(new MouseDownHandler() {
            public void onMouseDown(MouseDownEvent event) {
                if (currentFocus != null && currentFocus.isEdit()) {
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

        fPanel.addMouseMoveHandler(new MouseMoveHandler() {
            public void onMouseMove(MouseMoveEvent event) {
                if (_resizing) {
                    setStyleAttribute(resizeLine.getElement(), "left", (event.getClientX()) + "px");
                }
            }
        });

        fPanel.addMouseUpHandler(new MouseUpHandler() {
            public void onMouseUp(MouseUpEvent event) {
                if (_resizing) {
                    cancelMove();

                    int selCol = (_leftGrow ? currentFocus.col - 1 : currentFocus.col);
                    if (selCol == -1) return;

                    int width = colSizes.get(selCol);
                    int offset = event.getClientX();

                    /**
                     * If we didn't move at all, don't calculate a new size.
                     */
                    if (offset - _startpos == 0) return;

                    colSizes.set(selCol, (width -= (_startpos -= event.getClientX())));

                    int currTableWidth = dataGrid.getScrollPanel().getOffsetWidth();

                    titleBar.getTable().getColumnFormatter().setWidth(selCol, width + "px");
                    dataGrid.getTable().getColumnFormatter().setWidth(selCol, width + "px");

                    titleBar.tableIndex.get(0).get(selCol).setWidth(width + "px");

                    for (int cX = 0; cX < dataGrid.tableIndex.size(); cX++) {
                        dataGrid.tableIndex.get(cX).get(selCol).setWidth(width + "px");
                    }

                    titleBar.getScrollPanel().setWidth(currTableWidth - 20 + "px");
                    dataGrid.getScrollPanel().setWidth(currTableWidth + "px");
                }
            }
        });


        fPanel.addKeyDownHandler(new KeyDownHandler() {
            public void onKeyDown(KeyDownEvent event) {
                if (currentFocus == null || currentFocus.edit) {
                    return;
                }

                switch (event.getNativeKeyCode()) {
                    case KeyCodes.KEY_TAB:
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
                        if (currentFocus.getRow() > 0)
                            dataGrid.tableIndex.get(currentFocus.getRow() - 1).get(currentFocus.getCol()).focus();
                        break;
                    case 63235:
                    case KeyCodes.KEY_RIGHT:
                        if (currentFocus.getCol() < cols - 1) {
                            if (currentFocusRowColSpan) {
                                titleBar.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() + 1).focus();
                            }
                            else {
                                dataGrid.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() + 1).focus();
                            }
                        }
                        break;
                    case 63233:
                    case KeyCodes.KEY_ENTER:
                    case KeyCodes.KEY_DOWN:
                        if (currentFocus.getRow() < dataGrid.tableIndex.size())
                            dataGrid.tableIndex.get(currentFocus.getRow() + 1).get(currentFocus.getCol()).focus();
                        break;
                    case 63234:
                    case KeyCodes.KEY_LEFT:
                        if (currentFocus.getCol() > 0) {
                            if (currentFocusRowColSpan) {
                                titleBar.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() - 1).focus();
                            }
                            else {
                                dataGrid.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() - 1).focus();
                            }
                        }
                        break;

                    case 63272:
                    case KeyCodes.KEY_DELETE:
                        currentFocus.setValue("");
                        break;

                    case 32: // spacebar
                        currentFocus.edit();
                        break;
                }


            }
        });


        Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
            public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
                if (currentFocus != null && currentFocus.isEdit()) {
                    return;
                }

                switch (event.getTypeInt()) {
                    case Event.ONKEYDOWN:
                        switch (event.getNativeEvent().getKeyCode()) {
                            case KeyCodes.KEY_TAB:
                                if (currentFocus != null && currentFocus.isEdit()) {
                                    currentFocus.stopedit();
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
        titleBar.getTableIndex().get(row).get(column).setValue(html);
    }

    public void setCell(int row, int column, String html) {
        cols = dataGrid.ensureRowsAndCols(row + 1, column + 1);
        dataGrid.getTableIndex().get(row).get(column).setValue(html);
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public int getCols() {
        return cols;
    }

    private int checkWidth(int column) {
        if (columnWidths.size() - 1 < column) {
            for (int i = 0; i <= column; i++) {
                columnWidths.add(150);
            }
        }

        return columnWidths.get(column);
    }

    public void updateWidth(int column, int width) {
        HTMLTable.ColumnFormatter colFormatter = titleBar.getTable().getColumnFormatter();
        colFormatter.setWidth(column, width + "px");

        // colFormatter = dataGrid.getTable().getColumnFormatter();
        dataGrid.getTable().getColumnFormatter().setWidth(column, width + "px");

        checkWidth(column);
        columnWidths.set(column, width);
    }

    public WSCell getCell(int row, int col) {
        return dataGrid.getCell(row, col);
    }

    private void selectColumn(int col) {
        for (ArrayList<WSCell> row : titleBar.getTableIndex()) {
            row.get(col).addStyleDependentName("hcolselect");
        }

        for (ArrayList<WSCell> row : dataGrid.getTableIndex()) {
            row.get(col).addStyleDependentName("colselect");
        }
    }

    private void blurColumn(int col) {
        for (ArrayList<WSCell> row : titleBar.getTableIndex()) {
            row.get(col).removeStyleDependentName("hcolselect");
        }

        for (ArrayList<WSCell> row : dataGrid.getTableIndex()) {
            row.get(col).removeStyleDependentName("colselect");
        }
    }

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

        public void addCell(int row, HTML w) {
            int currentColSize = table.getCellCount(row);
            table.addCell(row);
            table.setWidget(row, currentColSize, new WSCell(this, w, row, currentColSize));
        }

        public void addRow() {
            table.insertRow(table.getRowCount());
            for (int i = 0; i < cols; i++) {
                addCell(table.getRowCount() - 1, new HTML());
            }
        }

        public int ensureRowsAndCols(int rows, int cols) {
            if (colSizes.size() < cols) {
                for (int i = 0; i < cols; i++) {
                    colSizes.add(200);
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
                        addCell(r, new HTML());
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
            _sort(col, ascending, 0, tableIndex.size() - 1);
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
                    if (ll.charAt(i) < rr.charAt(i))  {
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
            String t;
            for (int z = 0; z < cols; z++) {
                t = valueAt(i, z);
                setValueAt(i, z, valueAt(j, z));
                setValueAt(j, z, t);
            }
        }

        public WSCell cellAt(int row, int col) {
            return tableIndex.size() > row ? tableIndex.get(row).get(col) : null;
        }

        public String valueAt(int row, int col) {
            return tableIndex.get(row).get(col).wrappedWidget.getHTML();
        }

        public void setValueAt(int row, int col, String html) {
            cellAt(row, col).setValue(html);
        }


        public ScrollPanel getScrollPanel() {
            return scrollPanel;
        }

    } //end WSAbstractGrid


    private static TextBox textBox;
    private boolean _msie_compatibility = getUserAgent().contains("msie");

    static {
        textBox = new TextBox();
        textBox.setStylePrimaryName("WSCell-editbox");
        textBox.setVisible(false);

        RootPanel.get().add(textBox);
    }

    public class WSCell extends Composite {
        private FlowPanel panel;

        private HTML wrappedWidget;

        private boolean numeric;
        private boolean edit;

        private int row;
        private int col;

        private WSAbstractGrid grid;

        public WSCell(WSAbstractGrid grid, HTML widget, int row, int column) {
            this.grid = grid;
            panel = new FlowPanel();
            panel.setStyleName("WSCell-panel");

            textBox.addKeyPressHandler(new KeyPressHandler() {
                public void onKeyPress(KeyPressEvent event) {
                    switch (event.getCharCode()) {
                        case KeyCodes.KEY_TAB:
                            stopedit();
                            break;
                        case KeyCodes.KEY_ENTER:
                            stopedit();
                            break;
                    }
                }
            });

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

            this.wrappedWidget = widget;
            panel.add(wrappedWidget);

            this.row = row;
            this.col = column;

            if (_msie_compatibility) {
                wrappedWidget.setHTML("&nbsp;");
            }

            initWidget(panel);
            setWidth(colSizes.get(column) + "px");
            setStyleName("WSCell");

            sinkEvents(Event.MOUSEEVENTS | Event.FOCUSEVENTS | Event.ONCLICK | Event.ONDBLCLICK);
        }

        /**
         * Calling this method will place the cell into edit mode.
         */
        public void edit() {
            String text = wrappedWidget.getHTML();

            if (_msie_compatibility && text.equals("&nbsp;")) text = "";

            textBox.setText(text);
            textBox.setVisible(true);

            Style s = textBox.getElement().getStyle();

            s.setProperty("left", getAbsoluteLeft() + "px");
            s.setProperty("top", getAbsoluteTop() + "px");

            textBox.setSize(getOffsetWidth() + "px", getOffsetHeight() + "px");

            edit = true;

            textBox.setCursorPos(textBox.getText().length());
            textBox.setFocus(true);
        }

        public void stopedit() {
            if (edit) {
                setValue(textBox.getText());

                textBox.setVisible(false);

                edit = false;
                fPanel.setFocus(true);
            }
        }

        public void blur() {
            stopedit();
            removeStyleDependentName("selected");

            if (currentFocusRowColSpan) {
                blurColumn(col);
                currentFocusRowColSpan = false;
            }
        }

        public void focus() {
            if (currentFocus != null && currentFocus != this) {
                currentFocus.blur();
            }

            boolean isFocus = currentFocus == this;

            currentFocus = this;

            if (grid.type == GridType.TITLEBAR) {
                currentFocusRowColSpan = true;

                if (!isFocus) {
                    selectColumn(col);
                }
                else if (!_resizeArmed) {
                    boolean asc;
                    if (sortedColumns.containsKey(col)) {
                        sortedColumns.put(col, asc = !sortedColumns.get(col));
                    }
                    else {
                        sortedColumns.put(col, asc = true);
                    }

                    dataGrid.sort(col, asc);
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
            if (_msie_compatibility && html.trim().length() == 0) {
                wrappedWidget.setHTML("&nbsp;");
            }
            else {
                wrappedWidget.setHTML(html);
            }

            numeric = isNumeric(html);
        }

        public String getValue() {
            return wrappedWidget.getHTML();
        }

        public HTML getWrappedWidget() {
            return wrappedWidget;
        }

        public Style getStyle() {
            return wrappedWidget.getElement().getStyle();
        }

        @Override
        public void onBrowserEvent(Event event) {
            int leftG = getAbsoluteLeft() + 10;
            int rightG = getAbsoluteLeft() + colSizes.get(col) - 10;

            switch (event.getTypeInt()) {
                case Event.ONMOUSEOVER:
                    if (!_resizing) addStyleDependentName("hover");
                    break;
                case Event.ONMOUSEOUT:
                    if (!_resizing) removeStyleDependentName("hover");
                    break;

                case Event.ONMOUSEMOVE:
                    if (!_resizing) {
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

                    focus();
                    break;

                case Event.ONFOCUS:
                    break;

                case Event.ONCLICK:
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

    private void cancelMove() {
        resizeLine.hide();
        _resizing = false;
        _resizeArmed = false;
    }

    public int getTitlebarOffsetHeight() {
        return titleBar.getOffsetHeight();
    }

    public void setHeight(String height) {
        panel.setHeight(height);
    }

    public void setPreciseHeight(int height) {
        int offsetHeight = height - getTitlebarOffsetHeight();
        setHeight(height + "px");
        dataGrid.getScrollPanel().setHeight(offsetHeight + "px");
    }

    public void setWidth(String width) {
        panel.setWidth(width);
    }

    public void setPreciseWidth(int width) {
        setWidth(width + "px");
        titleBar.getScrollPanel().setWidth(width - 20 + "px");
        dataGrid.getScrollPanel().setWidth(width + "px");
    }

    public void growWidth(int amount) {
        int newWidth = dataGrid.getScrollPanel().getOffsetWidth() + amount;
        setWidth(newWidth + "px");
        titleBar.getScrollPanel().setWidth(newWidth - 20 + "px");
        dataGrid.getScrollPanel().setWidth(newWidth + "px");
    }

    @Override
    protected void onAttach() {
        int titleHeight = titleBar.getOffsetHeight();

        panel.setCellHeight(titleBar, titleHeight + "px");

        setHeight("450px");
        setWidth("100%");
        dataGrid.setHeight("450px");

        super.onAttach();
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
