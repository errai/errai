package org.jboss.errai.widgets.client.listeners;

import com.google.gwt.event.dom.client.ChangeEvent;
import org.jboss.errai.widgets.client.WSGrid;
import org.jboss.errai.widgets.client.format.WSCellFormatter;

public class CellChangeEvent extends ChangeEvent {
    private WSGrid.WSCell cell;
    private Object newValue;

    public CellChangeEvent(WSGrid.WSCell cell, Object newValue) {
        this.cell = cell;
        this.newValue = newValue;
    }

    public WSGrid.WSCell getCell() {
        return cell;
    }

    public Object getNewValue() {
        return newValue;
    }

    public Object getOldValue() {
        return cell.getCellFormat().getValue();
    }
}
