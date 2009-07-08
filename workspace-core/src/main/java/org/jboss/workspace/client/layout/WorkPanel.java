package org.jboss.workspace.client.layout;

import com.google.gwt.user.client.ui.*;


public class WorkPanel extends Composite  {
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

        getElement().getStyle().setProperty("overflow", "scroll");

        final WorkPanel workPanel = this;
        LayoutHint.attach(mainPanel, new LayoutHintProvider() {
            public int getHeightHint() {
                int height =  workPanel.getOffsetHeight() - title.getOffsetHeight();

                return height;
            }

            public int getWidthHint() {
                int width =  workPanel.getOffsetWidth();

                return width;
            }
        });
    }

    @Override
    public void setPixelSize(int i, int i1) {
        super.setPixelSize(i, i1);
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
}
