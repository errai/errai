package org.jboss.errai.client.listeners;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.Window;
import org.jboss.errai.client.framework.MessageCallback;
import org.jboss.errai.client.framework.Tool;
import org.jboss.errai.client.layout.LayoutHint;
import org.jboss.errai.client.layout.LayoutHintProvider;
import org.jboss.errai.client.rpc.CommandMessage;
import org.jboss.errai.client.rpc.ConversationMessage;
import org.jboss.errai.client.rpc.MessageBusClient;
import org.jboss.errai.client.rpc.protocols.LayoutCommands;
import org.jboss.errai.client.rpc.protocols.LayoutParts;

public class TabOpeningClickHandler implements ClickHandler {
    private Tool tool;

    public TabOpeningClickHandler(Tool tool) {
        this.tool = tool;
    }

    public void onClick(ClickEvent event) {
        String initSubject = tool.getClass().getName() + ":init";

        MessageBusClient.subscribeOnce(initSubject, new MessageCallback() {
            public void callback(CommandMessage message) {
                try {
                    final Widget w = tool.getWidget();
                    w.getElement().setId(message.get(String.class, LayoutParts.DOMID));
                   // w.setVisible(false);
                    RootPanel.get().add(w);

                    LayoutHint.attach(w, new LayoutHintProvider() {
                        public int getHeightHint() {
                            return Window.getClientHeight() - w.getAbsoluteTop() - 20;
                        }

                        public int getWidthHint() {
                            return Window.getClientWidth() - w.getAbsoluteLeft() - 5;
                        }
                    });

                    MessageBusClient.store(ConversationMessage.create(message));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        /**
         * Being capturing all message registration activity. This is necessary if you want to use the automatic
         * clean-up features and close the messaging channels when the tool instance closes.
         */
        MessageBusClient.beginCapture();

        MessageBusClient.store("org.jboss.errai.WorkspaceLayout", CommandMessage.create(LayoutCommands.OpenNewTab)
                        .set(LayoutParts.ComponentID, tool.getId())
                        .set(LayoutParts.IconURI, tool.getIcon().getUrl())
                        .set(LayoutParts.MultipleInstances, tool.multipleAllowed())
                        .set(LayoutParts.Name, tool.getName())
                        .set(LayoutParts.DOMID, tool.getId() + "_" + System.currentTimeMillis())
                        .set(LayoutParts.InitSubject, initSubject));
    }

}
