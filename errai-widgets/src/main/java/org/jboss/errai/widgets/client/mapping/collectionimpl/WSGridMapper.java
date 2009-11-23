/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.widgets.client.mapping.collectionimpl;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import org.jboss.errai.widgets.client.WSGrid;
import org.jboss.errai.widgets.client.format.WSCellFormatter;
import org.jboss.errai.widgets.client.listeners.CellChangeEvent;
import org.jboss.errai.widgets.client.mapping.ColumnMapper;
import org.jboss.errai.widgets.client.mapping.FieldMapper;
import org.jboss.errai.widgets.client.mapping.CollectionWidgetMapper;

import java.util.List;

public class WSGridMapper<V extends List<X>, X> extends CollectionWidgetMapper<WSGrid, WSCellFormatter, V> {
    private WSGrid grid;
    private List<X> list;
    private boolean noupdate = false;

    public WSGridMapper(WSGrid grid) {
        this.grid = grid;
        this.grid.addAfterCellChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                if (noupdate) return;

                CellChangeEvent evt = (CellChangeEvent) event;

                if (list != null) {
                    X v = list.get(evt.getCell().getOriginalRow());
                    fields[evt.getCell().getCol()].setFieldValue(evt.getCell().getCellFormat(), v);
                    fireAllChangeHandlers(v);
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
        noupdate = true;
        grid.clear();
        
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

        noupdate = false;
    }

    public X getSelected() {
        int currSelRow = grid.getSelectionList().iterator().next().getOriginalRow();
        return list.get(currSelRow);
    }
}
