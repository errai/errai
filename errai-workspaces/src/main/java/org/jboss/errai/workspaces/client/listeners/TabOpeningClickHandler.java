/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.workspaces.client.listeners;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.bus.client.*;
import org.jboss.errai.bus.client.protocols.LayoutCommands;
import org.jboss.errai.workspaces.client.protocols.LayoutParts;
import org.jboss.errai.workspaces.client.framework.Tool;
import org.jboss.errai.workspaces.client.layout.LayoutHint;
import org.jboss.errai.workspaces.client.layout.LayoutHintProvider;

import static org.jboss.errai.bus.client.MessageBuilder.createConversation;
import static org.jboss.errai.bus.client.MessageBuilder.createMessage;

public class TabOpeningClickHandler implements ClickHandler {
    private Tool tool;

    public TabOpeningClickHandler(Tool tool) {
        this.tool = tool;
    }

    public void onClick(ClickEvent event) {
        String initSubject = tool.getId() + ":init";

        final MessageBus bus = ErraiBus.get();

        if (!bus.isSubscribed(initSubject)) {
            bus.subscribe(initSubject, new MessageCallback() {
                public void callback(Message message) {

                    try {
                        final Widget w = tool.getWidget();
                        w.getElement().setId(message.get(String.class, LayoutParts.DOMID));

                        RootPanel.get().add(w);

                        LayoutHint.attach(w, new LayoutHintProvider() {
                            public int getHeightHint() {
                                return Window.getClientHeight() - w.getAbsoluteTop() - 20;
                            }

                            public int getWidthHint() {
                                return Window.getClientWidth() - w.getAbsoluteLeft() - 5;
                            }
                        });

                        createConversation(message).getMessage().sendNowWith(bus);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }
        try {

        /**
         * Being capturing all message registration activity. This is necessary if you want to use the automatic
         * clean-up features and close the messaging channels when the tool instance closes.
         */
        ((ClientMessageBus)bus).beginCapture();

        createMessage()
                .toSubject("org.jboss.errai.WorkspaceLayout")
                .command(LayoutCommands.OpenNewTab)
                .with(LayoutParts.ComponentID, tool.getId())
                .with(LayoutParts.IconURI, tool.getIcon().getUrl())
                .with(LayoutParts.MultipleInstances, tool.multipleAllowed())
                .with(LayoutParts.Name, tool.getName())
                .with(LayoutParts.DOMID, tool.getId() + "_" + System.currentTimeMillis())
                .with(LayoutParts.InitSubject, initSubject)
                .noErrorHandling().sendNowWith(bus);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
