package org.jboss.workspace.client.widgets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import org.jboss.workspace.client.Workspace;
import org.jboss.workspace.client.layout.WorkspaceLayout;
import org.jboss.workspace.client.listeners.TabCloseListener;
import org.jboss.workspace.client.rpc.StatePacket;
import org.jboss.workspace.client.util.Effects;
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

    final HorizontalPanel hPanel = new HorizontalPanel();

    public WSTab(WorkspaceLayout bl, Widget widgetRef, Image tabIcon, StatePacket packet,
                        WSTabPanel tabPanel) {
        super();

        this.layout = bl;
        this.packet = packet;
        this.widgetRef = widgetRef;

        initWidget(hPanel);

        hPanel.add(tabIcon);

        label = new Label(packet.getName());
        label.setStylePrimaryName("workspace-TabLabelText");

        hPanel.add(label);

        Image closeButton = new Image("images/close-icon.png");
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


    private AnimationTimer animTimer;

    @Override
    public void onBrowserEvent(Event event) {
    }


    public void blink() {
         if (animTimer != null) animTimer.scheduleRepeating(20);
    }

    public void stopAnimation() {
        if (animTimer != null) animTimer.setRunning(false);
    }

    /**
     * This class implements the pulsing fade-in/fade-out effect for tabs.
     */
    public static class AnimationTimer extends Timer {
        float i = 0.5f;
        float step = 0.02f;
        float target = 1.0f;

        boolean up = true;
        boolean running = true;
        boolean _running = true;

        WSTab wt;
        HorizontalPanel hp;
        Style s;

        public AnimationTimer(WSTab wt, HorizontalPanel hp) {
            this.wt = wt;
            this.s = hp.getElement().getParentElement().getStyle();
            this.hp = hp;
        }

        public void run() {
            if (up) {
                i += step;
                if (i >= 1.0f) {
                    i = 1.0f;
                    up = false;

                    if (!running) {
                        Effects.setOpacity(s, target);
                        _running = false;
                    }
                }
            }
            else {
                i -= step;
                if (i <= 0.5f) {
                    i = 0.5f;
                    up = true;
                }
            }

            Effects.setOpacity(s, i);
            if (!_running) {
                Effects.setOpacity(s, target);

                cancel();
            }
        }

        public void schedule(int i) {
            this.running = this._running = true;
            super.schedule(i);
        }

        public void scheduleRepeating(int i) {
            this.running = this._running = true;
            super.scheduleRepeating(i);
        }

        public boolean isRunning() {
            return running;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        public void updateTarget() {
            if (wt.getElement().getParentElement().getClassName().contains("-selected")) {
                this.target = 1.0f;
            }
            else {
                this.target = 0.7f;
            }
        }

        public void setOpacityToTarget() {
            Effects.setOpacity(s, target);

        }
    }

    public void reset() {
        sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT);

        
        if (isAttached()) {
            animTimer = new AnimationTimer(this, hPanel);
        }
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

    public void activate() {
        layout.tabPanel.selectTab(layout.tabPanel.getWidgetIndex(widgetRef));
    }
}
