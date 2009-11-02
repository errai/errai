package org.jboss.errai.widgets.client.mapping;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import org.jboss.errai.widgets.client.WSGrid;
import org.jboss.errai.widgets.client.format.WSCellFormatter;
import org.jboss.errai.widgets.client.listeners.CellChangeEvent;

import java.util.List;

public class WSGridMapper<V extends List<X>, X> extends WidgetMapper<WSGrid, WSCellFormatter, V> {
    private WSGrid grid;
    private List<X> list;
    private String[] defaultTitleValues;

    public WSGridMapper(WSGrid grid) {
        this.grid = grid;
        this.grid.addAfterCellChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                CellChangeEvent evt = (CellChangeEvent) event;

                if (list != null)  {
                    X v = list.get(evt.getCell().getOriginalRow());
                    fields[evt.getCell().getCol()].setFieldValue(evt.getCell().getCellFormat(), v);
                    fireAreChangeHandlers(v);
                }
            }
        });
    }

    private ColumnMapper<WSGrid, WSCellFormatter, X> mapper = new ColumnMapper<WSGrid, WSCellFormatter, X>() {
        public void mapRow(int row, FieldMapper<WSGrid, WSCellFormatter, X>[] fields, WSGrid w, X value) {
            int col = 0;
            for (FieldMapper<WSGrid, WSCellFormatter, X> f : fields) {
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
    public void map(V list) {
        this.list = list;

        if (defaultTitleValues != null && grid.getCols() == 0) {
            int i = 0;
            for (String s : defaultTitleValues) {
                grid.setColumnHeader(0, i++, s);
            }
        }

        int row = 0;
        for (X o : list) {
            mapper.mapRow(row++, fields, grid, o);
        }
    }
}
