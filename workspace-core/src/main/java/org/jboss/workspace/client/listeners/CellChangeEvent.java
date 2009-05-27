package org.jboss.workspace.client.listeners;

import org.jboss.workspace.client.widgets.WSGrid;
import com.google.gwt.event.dom.client.ChangeEvent;

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
