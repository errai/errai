package org.jboss.errai.widgets.client.mapping.impl;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import org.jboss.errai.widgets.client.WSGrid;
import org.jboss.errai.widgets.client.format.WSCellFormatter;
import org.jboss.errai.widgets.client.listeners.CellChangeEvent;
import org.jboss.errai.widgets.client.mapping.ColumnMapper;
import org.jboss.errai.widgets.client.mapping.FieldMapper;
import org.jboss.errai.widgets.client.mapping.WidgetMapper;

import java.util.List;

public class WSGridMapper<V extends List<X>, X> extends WidgetMapper<WSGrid, WSCellFormatter, V> {
    private WSGrid grid;
    private List<X> list;


    public WSGridMapper(WSGrid grid) {
        this.grid = grid;
        this.grid.addAfterCellChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                CellChangeEvent evt = (CellChangeEvent) event;

                if (list != null) {
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


    @Override
    public void map(V list) {
        this.list = list;

        if (grid.getCols() == 0) {
            int i = 0;
            if (defaultTitleValues != null) {
                if (defaultTitleValues.length != fields.length) {
                    throw new RuntimeException("Different number of defaultTitle fields ("
                            + defaultTitleValues.length + ") from actual fields (" + fields.length + ")");
                }

                for (String s : defaultTitleValues) {
                    grid.setColumnHeader(0, i++, s);
                }
            }
        }

        int row = 0;
        for (X o : list) {
            mapper.mapRow(row++, fields, grid, o);
        }
    }
}
