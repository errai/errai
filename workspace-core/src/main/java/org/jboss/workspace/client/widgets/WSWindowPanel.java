package org.jboss.workspace.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import static com.google.gwt.user.client.DeferredCommand.addCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import static com.google.gwt.user.client.Window.getClientHeight;
import static com.google.gwt.user.client.Window.getClientWidth;
import com.google.gwt.user.client.ui.*;
import org.jboss.workspace.client.layout.WSDropShadowLayout;
import org.jboss.workspace.client.util.Effects;
import org.jboss.workspace.client.util.LayoutUtil;

import static java.lang.Math.round;
import java.util.LinkedList;
import java.util.List;

/**
 * Workspace Window Panel implementation.  Provides basic popup window facilities.
 */
public class WSWindowPanel extends Composite {
    private DockPanel dockPanel = new DockPanel();
    private Image icon = new Image(GWT.getModuleBaseURL() + "/images/ui/icons/flag_blue.png");
    private Label label = new Label("Workspace Popup");

    private int offsetX;
    private int offsetY;

    private boolean drag;

    private MouseMover mouseMover = new MouseMover();
    private HandlerRegistration mouseMoverReg;

    public static int zIndex = 1;

    private List<Window.ClosingHandler> closingHandlers;

    private WSWindowPanel windowPanel;

    public WSWindowPanel() {
        windowPanel = this;

        WSDropShadowLayout dropShadow = new WSDropShadowLayout(dockPanel);

        dockPanel.setStyleName("WSWindowPanel");
        dropShadow.getElement().getStyle().setProperty("position", "absolute");
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

        initWidget(dropShadow);
        setVisible(false);
        RootPanel.get().add(this);

        fPanel.addMouseDownHandler(new MouseDownHandler() {
            public void onMouseDown(MouseDownEvent event) {
                offsetX = event.getClientX() - windowPanel.getAbsoluteLeft();
                offsetY = event.getClientY() - windowPanel.getAbsoluteTop();
                drag = true;

                windowPanel.getElement().getStyle().setProperty("zIndex", zIndex++ + "");

                mouseMoverReg = Event.addNativePreviewHandler(mouseMover);

                Effects.setOpacity(windowPanel.getElement(), 50);
            }
        });

        fPanel.addMouseUpHandler(new MouseUpHandler() {
            public void onMouseUp(MouseUpEvent event) {
                drag = false;
                mouseMoverReg.removeHandler();

                Effects.setOpacity(windowPanel.getElement(), 100);
            }
        });

        LayoutUtil.disableTextSelection(label.getElement(), true);

        setHeight("25px");
        setWidth("25px");

        Effects.setOpacity(dropShadow.getElement(), 50);
    }

    public WSWindowPanel(String title) {
        this();
        setTitle(title);
    }

    public void hide() {
        Effects.fade(getElement(), 1, 5, 100, 0);
        setVisible(false);
        fireClosingHandlers();
    }

    public void show() {
        Effects.setOpacity(getElement(), 0);
        setVisible(true);
        Effects.fade(getElement(), 5, 10, 0, 100);

        getElement().getStyle().setProperty("zIndex", zIndex++ + "");
    }


    public void add(Widget w) {
        dockPanel.add(w, DockPanel.CENTER);
        dockPanel.setSpacing(0);
    }

    @Deprecated
    public void setWidget(Widget w) {
        add(w);
    }

    public void center() {
        final Style s = getElement().getStyle();

        /**
         * Defer the command to ensure calculations occur after all the DOM elements are attached.
         */
        addCommand(new Command() {
            public void execute() {
                int top = (int) round((((double) getClientHeight()) - ((double) getOffsetHeight())) / 2d);
                int left = (int) round((((double) getClientWidth()) - ((double) getOffsetWidth())) / 2d);

                s.setProperty("top", top + "px");
                s.setProperty("left", left + "px");
            }
        });
    }

    public void setIcon(String url) {
        icon.setUrl(url);
    }

    public void setTitle(String title) {
        label.setText(title);
    }

    public void addClosingHandler(Window.ClosingHandler closingHandler) {
        if (closingHandlers == null) closingHandlers = new LinkedList<Window.ClosingHandler>();
        closingHandlers.add(closingHandler);
    }

    private void fireClosingHandlers() {
        if (closingHandlers != null) {
            WSClosingEvent event = new WSClosingEvent(this);
            for (Window.ClosingHandler handler : closingHandlers) {
                handler.onWindowClosing(event);
            }
        }
    }

    public class MouseMover implements Event.NativePreviewHandler {
        public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
            if (event.getTypeInt() == Event.ONMOUSEMOVE && drag) {
                Style s = windowPanel.getElement().getStyle();

                s.setProperty("top", (event.getNativeEvent().getClientY() - offsetY) + "px");
                s.setProperty("left", (event.getNativeEvent().getClientX() - offsetX) + "px");

                event.cancel();
            }
        }
    }

    public class WSClosingEvent extends Window.ClosingEvent {
        private WSWindowPanel source;

        public WSClosingEvent(WSWindowPanel source) {
            super();
            this.source = source;
        }

        public WSWindowPanel getSource() {
            return source;
        }
    }
}
