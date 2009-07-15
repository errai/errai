package org.jboss.workspace.client.layout;

import com.google.gwt.user.client.ui.*;


public class WorkPanel extends Composite  {
    VerticalPanel vPanel = new VerticalPanel();
    private HorizontalPanel title = new HorizontalPanel();
    private FlowPanel mainPanel = new FlowPanel();

    private int h;
    private int w;

    public WorkPanel() {
        vPanel.setWidth("100%");
   //     title.setWidth("100%");

        vPanel.add(title);
        vPanel.add(mainPanel);

        title.setHeight("25px");
        vPanel.setCellHeight(title, "25px");
     //   vPanel.setCellWidth(title, "100%");

        title.setStyleName("WS-WorkPanel-title");
        vPanel.setStyleName("WS-WorkPanel-area");

        initWidget(vPanel);

        getElement().getStyle().setProperty("overflow", "scroll");
    }

    @Override
    public void setPixelSize(int width, int height) {
    //    System.out.println("WorkPanel.setPixelSize(width:" + width + ", height:" + height + ")");

        h = (height - title.getOffsetHeight());
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
        title.add(w);
    }

    public int getPanelWidth() {
        return w == 0 ? getOffsetWidth() : w;
    }

    public int getPanelHeight() {
        return h == 0 ? getOffsetHeight() - title.getOffsetHeight() : h;
    }

    @Override
    public void setTitle(String s) {
        title.add(new Label(s));
    }
}
