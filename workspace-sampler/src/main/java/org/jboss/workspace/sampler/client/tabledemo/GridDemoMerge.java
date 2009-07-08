package org.jboss.workspace.sampler.client.tabledemo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.Window;
import org.jboss.workspace.client.framework.Tool;
import org.jboss.workspace.client.framework.WorkspaceSizeChangeListener;
import org.jboss.workspace.client.framework.AcceptsCallback;
import org.jboss.workspace.client.layout.WorkPanel;
import org.jboss.workspace.client.rpc.StatePacket;
import org.jboss.workspace.client.widgets.WSGrid;
import org.jboss.workspace.client.widgets.WSModalDialog;


public class GridDemoMerge implements Tool {
    public Widget getWidget(final StatePacket packet) {
        final WorkPanel workPanel = new WorkPanel();
        workPanel.setHeight("100%");
        workPanel.setTitle("Grid Demo Merge");

        final WSGrid grid = new WSGrid();
        grid.setWidth("100%");

        grid.setColumnHeader(0, 0, "A");
        grid.setColumnHeader(0, 1, "B");
        grid.setColumnHeader(0, 2, "C");

        grid.setCell(0, 0, "1");
        grid.setCell(0, 1, "2");
        grid.setCell(0, 2, "3");

        grid.setCell(1, 0, "2");
        grid.setCell(1, 1, "4");
        grid.setCell(1, 2, "6");

        grid.setCell(2, 0, "3");
        grid.setCell(2, 1, "6");
        grid.setCell(2, 2, "9");


        Button mergeButton = new Button("Merge");
        mergeButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                WSGrid.WSCell startCell = grid.getSelectionList().firstElement();
                WSGrid.WSCell endCell = grid.getSelectionList().lastElement();

                startCell.mergeColumns(endCell.getCol() - startCell.getCol());
            }
        });

        workPanel.add(mergeButton);

        Button dialogButton = new Button("Open...");
        dialogButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                WSModalDialog modal = new WSModalDialog();
                modal.ask("Are you sure", new AcceptsCallback() {
                    public void callback(String message) {
                       if ("OK".equals(message)) Window.alert("Response was OK");
                    }
                });

                modal.showModal();

            }
        });
        workPanel.add(dialogButton);

        workPanel.add(grid);
        
        return workPanel;
    }

    public String getName() {
        return "Grid Merge Demo";
    }

    public String getId() {
        return "gridMergeDemo";
    }

    public Image getIcon() {
        return new Image(GWT.getModuleBaseURL() + "/images/ui/icons/table_lightning.png");
    }

    public boolean multipleAllowed() {
        return true;
    }
}
