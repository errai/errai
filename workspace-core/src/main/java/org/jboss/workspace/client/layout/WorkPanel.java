package org.jboss.workspace.client.layout;

import com.google.gwt.user.client.ui.*;

public class WorkPanel extends Composite {
    VerticalPanel vPanel = new VerticalPanel();

    private Label titleLabel = new Label("New WorkPanel");
    private SimplePanel title = new SimplePanel();
    private HorizontalPanel titleInternal = new HorizontalPanel();
    private FlowPanel mainPanel = new FlowPanel();

    private int h;
    private int w;

    public WorkPanel() {
        vPanel.setWidth("100%");

        vPanel.add(title);
        vPanel.add(mainPanel);

        title.setHeight("25px");
        vPanel.setCellHeight(title, "25px");

        titleLabel.setStyleName("WS-WorkPanel-title-label");
        title.setStyleName("WS-WorkPanel-title");
        vPanel.setStyleName("WS-WorkPanel-area");

        titleInternal.add(titleLabel);
        title.setWidget(titleInternal);

        initWidget(vPanel);

        getElement().getStyle().setProperty("overflow", "scroll");
    }

    @Override
    public void setPixelSize(int width, int height) {
        h = (height - titleInternal.getOffsetHeight());
        w = width;

        vPanel.setCellHeight(mainPanel, h + "px");
        vPanel.setCellWidth(mainPanel, width + "px");

        vPanel.setPixelSize(width, height);
        super.setPixelSize(width, height);
    }

    public void add(Widget w) {
        mainPanel.add(w);
    }

    public void addToTitlebar(Widget w) {
        titleInternal.add(w);
        titleInternal.setCellHorizontalAlignment(w, HasHorizontalAlignment.ALIGN_LEFT);
    }

    public int getPanelWidth() {
        return w == 0 ? getOffsetWidth() : w;
    }

    public int getPanelHeight() {
        return h == 0 ? getOffsetHeight() - titleInternal.getOffsetHeight() : h;
    }

    @Override
    public void setTitle(String s) {
        titleLabel.setText(s);
    }
}
