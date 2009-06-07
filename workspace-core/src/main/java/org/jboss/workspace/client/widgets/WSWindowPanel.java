package org.jboss.workspace.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import static com.google.gwt.user.client.Window.getClientHeight;
import static com.google.gwt.user.client.Window.getClientWidth;
import com.google.gwt.user.client.ui.*;
import org.jboss.workspace.client.util.Effects;

public class WSWindowPanel extends Composite {
    private DockPanel dockPanel = new DockPanel();
    private Image icon = new Image(GWT.getModuleBaseURL() + "/images/ui/icons/flag_blue.png");
    private Label label = new Label("Workspace Popup");

    private int offsetX;
    private int offsetY;

    private boolean drag;

    private Style windowStyle;
    private MouseMover mouseMover = new MouseMover();
    private HandlerRegistration mouseMoverReg;

    private static int zIndex = 1;

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

        FocusPanel fPanel = new FocusPanel();
        fPanel.add(titleArea);

        dockPanel.add(fPanel, DockPanel.NORTH);
        dockPanel.setCellVerticalAlignment(fPanel, HasVerticalAlignment.ALIGN_MIDDLE);

        initWidget(dockPanel);
        setVisible(false);
        RootPanel.get().add(this);

        final WSWindowPanel windowPanel = this;
        windowStyle = getElement().getStyle();

        fPanel.addMouseDownHandler(new MouseDownHandler() {
            public void onMouseDown(MouseDownEvent event) {
                offsetX = event.getClientX() - windowPanel.getAbsoluteLeft();
                offsetY = event.getClientY() - windowPanel.getAbsoluteTop();
                drag = true;

                windowStyle.setProperty("zIndex", zIndex++ + "");

                mouseMoverReg = Event.addNativePreviewHandler(mouseMover);

                Effects.setOpacity(windowStyle, 50);
            }
        });

        fPanel.addMouseUpHandler(new MouseUpHandler() {
            public void onMouseUp(MouseUpEvent event) {
                drag = false;
                mouseMoverReg.removeHandler();

                Effects.setOpacity(windowStyle, 100);
            }
        });

    }

    public void hide() {
        Effects.fade(getElement(), 1, 5, 100, 0);
        setVisible(false);
    }

    public void show() {
        Effects.setOpacity(windowStyle, 0);
        setVisible(true);
        Effects.fade(getElement(), 1, 5, 0, 100);

        windowStyle.setProperty("zIndex", zIndex++ + "");
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

    public class MouseMover implements Event.NativePreviewHandler {
        public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
            if (event.getTypeInt() == Event.ONMOUSEMOVE && drag) {
                windowStyle.setProperty("top", (event.getNativeEvent().getClientY() - offsetY) + "px");
                windowStyle.setProperty("left", (event.getNativeEvent().getClientX() - offsetX) + "px");
            }
        }
    }


}
