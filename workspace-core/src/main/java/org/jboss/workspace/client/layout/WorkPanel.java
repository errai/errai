package org.jboss.workspace.client.layout;

import com.google.gwt.user.client.ui.*;
import org.jboss.workspace.client.widgets.HeightAware;


public class WorkPanel extends Composite implements HeightAware {
    VerticalPanel vPanel = new VerticalPanel();
    private FlowPanel title = new FlowPanel();
    private FlowPanel mainPanel = new FlowPanel();

    public WorkPanel() {
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

    @Override
    public void setTitle(String s) {
        title.add(new Label(s));
    }

    public int getComponentHeight() {
        return mainPanel.getOffsetHeight() - title.getOffsetHeight();
    }
}
