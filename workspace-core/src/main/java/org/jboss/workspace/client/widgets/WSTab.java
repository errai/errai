package org.jboss.workspace.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;
import org.jboss.workspace.client.layout.WorkspaceLayout;
import org.jboss.workspace.client.listeners.TabCloseListener;
import org.jboss.workspace.client.rpc.StatePacket;
import org.jboss.workspace.client.widgets.dnd.TabDropController;


/**
 * A WorkspaceTab is the actual implementation of the rendered tabs along the top of the workspace.
 */
public class WSTab extends Composite {
    WorkspaceLayout layout;
    StatePacket packet;

    Widget widgetRef;
    final Label label;
    TabDropController tabDropController;
    Image icon;

    final HorizontalPanel hPanel = new HorizontalPanel();

    public WSTab(WorkspaceLayout bl, Widget widgetRef, Image tabIcon, StatePacket packet,
                 WSTabPanel tabPanel) {
        super();

        this.layout = bl;
        this.packet = packet;
        this.widgetRef = widgetRef;
        this.icon = tabIcon;

        initWidget(hPanel);

        hPanel.add(tabIcon);

        label = new Label(packet.getName());
        label.setStylePrimaryName("workspace-TabLabelText");

        hPanel.add(label);

        Image closeButton = new Image(GWT.getModuleBaseURL() + "/images/close-icon.png");
        closeButton.addStyleName("workspace-tabCloseButton");
        closeButton.addClickListener(new TabCloseListener(this, bl));

        hPanel.add(closeButton);

        reset();

        this.tabDropController = new TabDropController(tabPanel, this);

        if (packet.isModifiedFlag()) decorateModified();
    }

    public boolean isModified() {
        return packet.isModifiedFlag();
    }

    public void setModified(boolean modified) {
        if (!packet.isModifiedFlag()) {
            packet.setModifiedFlag(modified);
            decorateModified();
            layout.notifySessionState(packet);
        }
    }

    private void decorateModified() {
        label.getElement().getStyle().setProperty("color", "darkblue");
    }

    public StatePacket getPacket() {
        return packet;
    }

    public void setPacket(StatePacket packet) {
        this.packet = packet;
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
        return "WSTab:" + this.packet.getName();
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
        return layout.tabPanel.getWidgetIndex(widgetRef) == layout.tabPanel.getActiveTab();
    }

    public void activate() {
        layout.tabPanel.selectTab(layout.tabPanel.getWidgetIndex(widgetRef));
    }
}
