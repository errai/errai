package org.jboss.workspace.client.layout;

import com.google.gwt.user.client.ui.*;


public class WorkPanel extends Composite {
    private SimplePanel title = new SimplePanel();
    private FlowPanel mainPanel = new FlowPanel();

    public WorkPanel() {
        VerticalPanel vPanel = new VerticalPanel();
        vPanel.setHeight("100%");
        vPanel.setWidth("100%");

        vPanel.add(title);
        vPanel.add(mainPanel);

        title.setHeight("25px");
        vPanel.setCellHeight(title, "25px");

        title.setStyleName("WS-WorkPanel-title");
        vPanel.setStyleName("WS-WorkPanel-area");

        initWidget(vPanel);
    }

    public void add(Widget w) {
         mainPanel.add(w);
    }

    public void addWidgetTitle(Widget w) {
        title.add(w);
    }
}
