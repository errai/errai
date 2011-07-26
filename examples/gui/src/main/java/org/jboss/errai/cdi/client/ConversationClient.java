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
package org.jboss.errai.cdi.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.enterprise.client.cdi.api.Conversation;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.api.annotations.LoadTool;

import javax.inject.Inject;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Oct 7, 2010
 */
//@LoadTool(name = "Reply Client", group="Scopes")
public class ConversationClient implements WidgetProvider {

    @Inject
    MessageBus bus;

    @Inject
    Conversation conversation;

    public void provideWidget(ProvisioningCallback callback) {
        LayoutPanel panel = new LayoutPanel();

        panel.add(new Button("Begin conversation",
                new ClickHandler()
                {
                    public void onClick(ClickEvent clickEvent) {

                        conversation. begin();

                        MessageBuilder.createMessage()
                                .toSubject("wizard")
                                .command("first")
                                .with("text", "Hello ")
                                .done().sendNowWith(bus);

                    }
                })
        );

        panel.add(new Button("End conversation",
                new ClickHandler()
                {
                    public void onClick(ClickEvent clickEvent) {
                        MessageBuilder.createMessage()
                                .toSubject("wizard")
                                .command("last")
                                .with("text", "world!")
                                .done().sendNowWith(bus);

                        if(!conversation.hasEnded())
                            conversation.end();
                    }
                })
        );

        callback.onSuccess(panel);
    }
}
