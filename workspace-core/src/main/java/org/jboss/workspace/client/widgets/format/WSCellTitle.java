package org.jboss.workspace.client.widgets.format;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.workspace.client.widgets.WSGrid;


public class WSCellTitle extends WSCellFormatter {
    HorizontalPanel hPanel = new HorizontalPanel();
    HTML label = new HTML();
    Image img = new Image();

    WSGrid.WSCell cell;

    public WSCellTitle(WSGrid.WSCell cell, String title) {
        this.cell = cell;

        label.setHTML(title);
        hPanel.add(label);
        hPanel.add(img);
        hPanel.setWidth("100%");
        hPanel.setCellWidth(img, "16px");

        img.setVisible(false);
        img.getElement().getStyle().setProperty("textAlign", "right");

    }

    public void setValue(String value) {
        label.setHTML(value);
    }

    public String getTextValue() {
        return label.getHTML();
    }

    public Widget getWidget(WSGrid wsGrid) {
        if (wsGrid.getSortedColumnHeader() == cell) {
            img.setUrl(GWT.getModuleBaseURL()
                    + "images/ui/icons/sort-" + (wsGrid.getColumnSortOrder(cell.getCol()) ? "down" : "up") + ".png");
            img.setVisible(true);
        }
        else {
            img.setVisible(false);
        }

        return hPanel;
    }

    public boolean edit(WSGrid.WSCell element) {
        return false;
    }

    public void stopedit() {
    }
}
