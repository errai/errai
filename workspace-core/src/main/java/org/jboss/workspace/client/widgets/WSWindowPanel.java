package org.jboss.workspace.client.widgets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import static com.google.gwt.user.client.Window.getClientHeight;
import static com.google.gwt.user.client.Window.getClientWidth;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.core.client.GWT;
import org.jboss.workspace.client.util.Effects;

public class WSWindowPanel extends Composite {
    private DockPanel dockPanel = new DockPanel();
    private Image icon = new Image(GWT.getModuleBaseURL() + "/images/ui/icons/flag_blue.png");
    private Label label = new Label("Workspace Popup");

    public WSWindowPanel() {
        dockPanel.getElement().getStyle().setProperty("position", "absolute");
        dockPanel.setStyleName("WSWindowPanel");

        /**
         * Build the window title area
         */
        HorizontalPanel titleArea = new HorizontalPanel();
        titleArea.setStyleName("WSWindowPanel-titlearea");
        titleArea.setWidth("100%");
        titleArea.setHeight("25px");

        titleArea.add(icon);
        titleArea.setCellVerticalAlignment(icon, HasVerticalAlignment.ALIGN_MIDDLE);
        titleArea.setCellWidth(icon, "18px");

        titleArea.add(label);
        titleArea.setCellVerticalAlignment(label, HasVerticalAlignment.ALIGN_MIDDLE);

        Button closeButton = new Button();
        closeButton.setHTML("<img src='" + GWT.getModuleBaseURL() + "/images/ui/icons/cancel.png' />");

        closeButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                hide();
            }
        });

        titleArea.add(closeButton);
        titleArea.setCellHorizontalAlignment(closeButton, HasHorizontalAlignment.ALIGN_RIGHT);

        dockPanel.add(titleArea, DockPanel.NORTH);
        dockPanel.setCellVerticalAlignment(titleArea, HasVerticalAlignment.ALIGN_MIDDLE);

        initWidget(dockPanel);
        setVisible(false);
        RootPanel.get().add(this);
    }

    public void hide() {
        Effects.fade(getElement(), 1, 5, 100, 0);
        setVisible(false);
    }

    public void show() {
        Effects.setOpacity(getElement().getStyle(), 0);
        setVisible(true);
        Effects.fade(getElement(), 1, 5, 0, 100);
    }

    public void add(Widget w) {
        dockPanel.add(w, DockPanel.CENTER);
        dockPanel.setSpacing(1);
    }

    public void center() {
        Style s = getElement().getStyle();

        int top = (int) Math.round((((double) getClientHeight()) - ((double) getOffsetHeight())) / 2d);
        int left = (int) Math.round((((double) getClientWidth()) - ((double) getOffsetWidth())) / 2d);

        s.setProperty("top", top + "px");
        s.setProperty("left", left + "px");
    }
}
