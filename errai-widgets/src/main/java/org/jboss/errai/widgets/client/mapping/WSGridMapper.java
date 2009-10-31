package org.jboss.errai.widgets.client.mapping;

import org.jboss.errai.widgets.client.WSGrid;
import org.jboss.errai.widgets.client.format.WSCellFormatter;

import java.util.List;

public class WSGridMapper<V> extends WidgetMapper<WSGrid, WSCellFormatter, V> {
    private WSGrid grid;
    private List<V> list;

    public WSGridMapper(WSGrid grid) {
        this.grid = grid;
    }

    private ColumnMapper<WSGrid, WSCellFormatter, V> mapper = new ColumnMapper<WSGrid, WSCellFormatter, V>() {
        public void mapRow(int row, FieldMapper<WSGrid, WSCellFormatter, V>[] fields, WSGrid w, V value) {
            int col = 0;
            for (FieldMapper<WSGrid, WSCellFormatter, V> f : fields) {
                 grid.setCell(row, col++, f.getFieldValue(w, value));
            }
        }
    };

    @Override
    public void map(List<V> list) {
        this.list = list;

        int row = 0;
        for (V o : list) {
            mapper.mapRow(row++, fields, grid, o);
        }
    }
}
