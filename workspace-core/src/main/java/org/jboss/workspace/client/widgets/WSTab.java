package org.jboss.workspace.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import org.jboss.workspace.client.widgets.dnd.TabDropController;

import java.util.List;
import java.util.ArrayList;


/**
 * A WorkspaceTab is the actual implementation of the rendered tabs along the top of the workspace.
 */
public class WSTab extends Composite {
    WSTabPanel panel;
    Widget widgetRef;

    final Label label;
    TabDropController tabDropController;
    Image icon;
    Image closeButton;

    boolean modifiedFlag = false;

    final HorizontalPanel hPanel = new HorizontalPanel();

    List<CloseHandler<WSTab>> tabCloseHandlers = new ArrayList<CloseHandler<WSTab>>();

    //todo: this widget is still tied to the Workspace API -- bad!
    public WSTab(String name, Widget widgetRef, Image tabIcon) {
        this.widgetRef = widgetRef;
        this.icon = tabIcon;

        initWidget(hPanel);

        hPanel.add(tabIcon);

        label = new Label(name);
        label.setStylePrimaryName("workspace-TabLabelText");

        hPanel.add(label);

        closeButton = new Image(GWT.getModuleBaseURL() + "/images/close-icon.png");
        closeButton.addStyleName("workspace-tabCloseButton");

        hPanel.add(closeButton);

        reset();
    }

    public boolean isModified() {
        return modifiedFlag;
    }

    public void setModified(boolean modified) {
        if (this.modifiedFlag = modified) decorateModified();
    }

    private void decorateModified() {
        label.getElement().getStyle().setProperty("color", "darkblue");
    }

    public Widget getWidgetRef() {
        return widgetRef;
    }

    public void setWidgetRef(Widget widgetRef) {
        this.widgetRef = widgetRef;
    }

    public void reset() {
        sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT);
    }

    @Override
    public String toString() {
        return "WSTab:" + label.getText();
    }

    public TabDropController getTabDropController() {
        return tabDropController;
    }

    public Label getLabel() {
        return label;
    }

    public Image getIcon() {
        return icon;
    }

    public boolean isActivated() {
        return panel.getWidgetIndex(widgetRef) == panel.getActiveTab();
    }

    public void activate() {
        panel.selectTab(panel.getWidgetIndex(widgetRef));
    }

    public int remove() {
        int idx = panel.getWidgetIndex(widgetRef);
        panel.remove(idx);
        return idx;
    }


    public void setPanel(WSTabPanel panel) {
        this.panel = panel;
        this.tabDropController = new TabDropController(panel, this);
//        closeButton.addClickHandler(new TabCloseHandler(this, bl));

    }

    public void addTabCloseHandler(CloseHandler<WSTab> closeHandler) {
        tabCloseHandlers.add(closeHandler);
    }

    private void notifyCloseHandlers()  {
        WSTabCloseEvent<WSTab> evt = new WSTabCloseEvent<WSTab>(this, false);
        for (CloseHandler<WSTab> handler : tabCloseHandlers) {
            handler.onClose(evt);
        }
    }

    public  class WSTabCloseEvent<T> extends CloseEvent<T> {
        protected WSTabCloseEvent(T target, boolean autoClosed) {
            super(target, autoClosed);
        }
    }

}
