package org.jboss.workspace.client.listeners;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.workspace.client.framework.AcceptsCallback;
import org.jboss.workspace.client.framework.CommandProcessor;
import org.jboss.workspace.client.framework.Tool;
import org.jboss.workspace.client.layout.LayoutHint;
import org.jboss.workspace.client.layout.LayoutHintProvider;
import org.jboss.workspace.client.rpc.MessageBusClient;

import java.util.HashMap;
import java.util.Map;

public class TabOpeningClickHandler implements ClickHandler {
    private Tool tool;

    public TabOpeningClickHandler(Tool tool) {
        this.tool = tool;
    }

    public void onClick(ClickEvent event) {
        /**
         * Build the message to send the command processor.
         */
        Map<String, Object> msg = new HashMap<String, Object>();
        msg.put(CommandProcessor.MessageParts.ComponentID.name(), tool.getId());
        msg.put(CommandProcessor.MessageParts.IconURI.name(), tool.getIcon().getUrl());
        msg.put(CommandProcessor.MessageParts.MultipleInstances.name(), tool.multipleAllowed());
        msg.put(CommandProcessor.MessageParts.Name.name(), tool.getName());

        final Widget w = tool.getWidget();

        /**
         * Generate a unique DOM ID for the actual Widget Element.  This is used to reference and pull in the actual
         * widget into the Workspace UI.
         */
        String DOMID = tool.getId() + "_" + System.currentTimeMillis();
        w.getElement().setId(DOMID);
        w.setVisible(false);
        RootPanel.get().add(w);

        msg.put(CommandProcessor.MessageParts.DOMID.name(), DOMID);

        String sizeHintsSubject = "org.jboss.workspace.sizehints." + DOMID;

        MessageBusClient.subscribe(sizeHintsSubject,
                new AcceptsCallback() {
                    public void callback(Object message, Object data) {
                        Map<String, Object> msg = MessageBusClient.decodeMap(message);

                        Double width = (Double) msg.get(CommandProcessor.MessageParts.Width.name());
                        Double height = (Double) msg.get(CommandProcessor.MessageParts.Height.name());

                        w.setPixelSize(width.intValue(), height.intValue());
                    }
                }, null);

        LayoutHint.attach(sizeHintsSubject, new LayoutHintProvider() {
            public int getHeightHint() {
                return Window.getClientHeight() - w.getAbsoluteTop() - 20;
            }

            public int getWidthHint() {
                return Window.getClientWidth() - w.getAbsoluteLeft() - 5;
            }
        });

        CommandProcessor.Command.OpenNewTab.send(msg);
    }

}
