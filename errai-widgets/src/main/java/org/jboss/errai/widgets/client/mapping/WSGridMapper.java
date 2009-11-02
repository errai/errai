package org.jboss.errai.widgets.client.mapping;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import org.jboss.errai.widgets.client.WSGrid;
import org.jboss.errai.widgets.client.format.WSCellFormatter;
import org.jboss.errai.widgets.client.listeners.CellChangeEvent;

import java.util.List;

public class WSGridMapper<V> extends WidgetMapper<WSGrid, WSCellFormatter, V> {
    private WSGrid grid;
    private List<V> list;
    private String[] defaultTitleValues;

    public WSGridMapper(WSGrid grid) {
        this.grid = grid;
        this.grid.addAfterCellChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                CellChangeEvent evt = (CellChangeEvent) event;

                if (list != null)  {
                    V v = list.get(evt.getCell().getOriginalRow());
                    fields[evt.getCell().getCol()].setFieldValue(evt.getCell().getCellFormat(), v);
                    fireAreChangeHandlers(v);
                }
            }
        });
    }

    private ColumnMapper<WSGrid, WSCellFormatter, V> mapper = new ColumnMapper<WSGrid, WSCellFormatter, V>() {
        public void mapRow(int row, FieldMapper<WSGrid, WSCellFormatter, V>[] fields, WSGrid w, V value) {
            int col = 0;
            for (FieldMapper<WSGrid, WSCellFormatter, V> f : fields) {
                 grid.setCell(row, col++, f.getFieldValue(w, value));
            }
        }
    };

    public String[] getDefaultTitleValues() {
        return defaultTitleValues;
    }

    public void setDefaultTitleValues(String[] defaultTitleValues) {
        this.defaultTitleValues = defaultTitleValues;
    }

    @Override
    public void map(List<V> list) {
        this.list = list;

        if (defaultTitleValues != null && grid.getCols() == 0) {
            int i = 0;
            for (String s : defaultTitleValues) {
                grid.setColumnHeader(0, i++, s);
            }
        }

        int row = 0;
        for (V o : list) {
            mapper.mapRow(row++, fields, grid, o);
        }
    }
}
