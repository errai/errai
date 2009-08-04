package org.jboss.workspace.sampler.client.tabledemo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import static com.google.gwt.i18n.client.DateTimeFormat.getShortDateFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.workspace.client.framework.Tool;
import org.jboss.workspace.client.framework.Federation;
import org.jboss.workspace.client.layout.LayoutHint;
import org.jboss.workspace.client.layout.LayoutHintProvider;
import org.jboss.workspace.client.layout.WorkPanel;
import org.jboss.workspace.client.rpc.StatePacket;
import org.jboss.workspace.client.widgets.WSGrid;
import org.jboss.workspace.client.widgets.format.WSCellDateFormat;
import org.jboss.workspace.client.widgets.format.WSCellMultiSelector;
import org.jboss.workspace.client.widgets.format.WSCellSimpleTextCell;

import java.util.LinkedHashSet;
import java.util.Set;


public class GridDemo implements Tool {

    public Widget getWidget(final StatePacket packet) {
        final WorkPanel workPanel = new WorkPanel();
        workPanel.setHeight("100%");

        final WSGrid wsGrid = new WSGrid();
        wsGrid.setHeight("100%");
        wsGrid.setWidth("100%");

        populateTable(wsGrid);

        assert packet.getActiveLayout() != null;

        wsGrid.addCellChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent changeEvent) {
                packet.setModifiedFlag(true);
            }
        });

        LayoutHint.attach(wsGrid, new LayoutHintProvider() {
            public int getHeightHint() {
                return workPanel.getPanelHeight();
            }

            public int getWidthHint() {
                return workPanel.getPanelWidth();
            }
        });

        workPanel.setTitle("Grid Demo");
        workPanel.add(wsGrid);


        Button mergeCells = new Button("Merge Cells");
        decorateUtilButton(mergeCells);
        mergeCells.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                wsGrid.mergeSelected();
            }
        });

        Button reset = new Button("Reset");
        decorateUtilButton(reset);
        reset.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                wsGrid.clear();
                populateTable(wsGrid);
            }
        });

        HorizontalPanel h = new HorizontalPanel();
        h.add(mergeCells);
        h.add(reset);

        workPanel.addToTitlebar(h);

        Federation.store("mysubject", "magpie");

        return workPanel;
    }

    private void decorateUtilButton(Button button) {
        button.setHeight("18px");
        button.getElement().getStyle().setProperty("fontSize", "10px");
        button.getElement().getStyle().setProperty("marginLeft", "5px");
    }

    private void populateTable(WSGrid wsGrid) {
        Set<String> userTypes = new LinkedHashSet<String>();
        userTypes.add("Regular User");
        userTypes.add("Super User");
        userTypes.add("Mark Proctor");

        wsGrid.setColumnHeader(0, 0, "UserId");
        wsGrid.setColumnHeader(0, 1, "Name");
        wsGrid.setColumnHeader(0, 2, "User Type");
        wsGrid.setColumnHeader(0, 3, "Date Created");

        wsGrid.setCell(0, 0, new WSCellSimpleTextCell("1", true));
        wsGrid.setCell(0, 1, "John Doe");
        wsGrid.setCell(0, 2, new WSCellMultiSelector(userTypes, "Regular User"));
        wsGrid.setCell(0, 3, new WSCellDateFormat(getShortDateFormat().parse("2/10/07")));

        wsGrid.setCell(1, 0, new WSCellSimpleTextCell("2", true));
        wsGrid.setCell(1, 1, "Jane Doe");
        wsGrid.setCell(1, 2, new WSCellMultiSelector(userTypes, "Mark Proctor"));
        wsGrid.setCell(1, 3, new WSCellDateFormat(getShortDateFormat().parse("5/20/05")));

        wsGrid.setCell(2, 0, new WSCellSimpleTextCell("3", true));
        wsGrid.setCell(2, 1, "Mike Rawlings");
        wsGrid.setCell(2, 2, new WSCellMultiSelector(userTypes, "Regular User"));
        wsGrid.setCell(2, 3, new WSCellDateFormat(getShortDateFormat().parse("1/2/01")));

        wsGrid.setCell(3, 0, new WSCellSimpleTextCell("4", true));
        wsGrid.setCell(3, 1, "Adam Smith");
        wsGrid.setCell(3, 2, new WSCellMultiSelector(userTypes, "Regular User"));
        wsGrid.setCell(3, 3, new WSCellDateFormat(getShortDateFormat().parse("9/17/02")));

        wsGrid.setCell(4, 0, new WSCellSimpleTextCell("5", true));
        wsGrid.setCell(4, 1, "Foo");
        wsGrid.setCell(4, 2, new WSCellMultiSelector(userTypes, "Super User"));
        wsGrid.setCell(4, 3, new WSCellDateFormat(getShortDateFormat().parse("7/15/06")));

    }

    public String getName() {
        return "Grid Demo";
    }

    public String getId() {
        return "gridDemo";
    }

    public Image getIcon() {
        return new Image(GWT.getModuleBaseURL() + "/images/ui/icons/table_lightning.png");
    }

    public boolean multipleAllowed() {
        return true;
    }
}

