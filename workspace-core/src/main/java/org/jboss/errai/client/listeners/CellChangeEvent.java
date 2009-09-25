package org.jboss.errai.client.listeners;

import com.google.gwt.event.dom.client.ChangeEvent;
import org.jboss.errai.client.widgets.WSGrid;

public class CellChangeEvent extends ChangeEvent {
    private WSGrid.WSCell cell;
    private String newValue;

    public CellChangeEvent(WSGrid.WSCell cell, String newValue) {
        this.cell = cell;
        this.newValue = newValue;
    }

    public WSGrid.WSCell getCell() {
        return cell;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getOldValue() {
        return cell.getValue();
    }
}
