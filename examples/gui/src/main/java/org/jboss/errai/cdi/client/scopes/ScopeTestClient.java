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
package org.jboss.errai.cdi.client.scopes;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import org.gwt.mosaic.ui.client.CaptionLayoutPanel;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.enterprise.client.cdi.api.Conversation;
import org.jboss.errai.enterprise.client.cdi.api.ConversationContext;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.api.annotations.LoadTool;

import java.util.List;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 6, 2010
 */
@LoadTool(name = "Scope Verification", group="Scopes")
public class ScopeTestClient implements WidgetProvider
{
    private final MessageBus bus = ErraiBus.get();

    private HTML responsePanel;

    @ConversationContext("wizard")     
    public Conversation conversation;

    public void provideWidget(ProvisioningCallback callback)
    {

        LayoutPanel panel = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));

        CaptionLayoutPanel requestScope = new CaptionLayoutPanel("Request Scope Beans");
        requestScope.setLayout(new BoxLayout(BoxLayout.Orientation.VERTICAL));

        Button button = new Button("Load categories", new ClickHandler()
        {
            public void onClick(ClickEvent clickEvent)
            {
                Categories call = MessageBuilder.createCall(
                        new RemoteCallback<List<String>>()
                        {
                            public void callback(List<String> values)
                            {
                                StringBuffer sb = new StringBuffer();
                                sb.append("<ul>");
                                for(String a : values)
                                    sb.append("<li>").append(a);
                                sb.append("</ul>");
                                responsePanel.setHTML(sb.toString());
                            }
                        }, Categories.class
                );

                call.getAllCategories();
            }
        });

        responsePanel = new HTML();

        requestScope.add(button);
        requestScope.add(responsePanel);

        // --------------------------------

        CaptionLayoutPanel conversationScope = new CaptionLayoutPanel("Reply Scope Beans");
        conversationScope.setLayout(new BoxLayout(BoxLayout.Orientation.VERTICAL));

        final TextBox textBox = new TextBox();
        final CheckBox requiresFlush = new CheckBox();
        final CheckBox requiresConversation = new CheckBox();
        requiresConversation.setValue(true);

        Button btn2 = new Button("Append to buffer", new ClickHandler()
        {
            public void onClick(ClickEvent clickEvent) {
                String text = textBox.getText();
                textBox.setText("");

                String command = requiresFlush.getValue() ? "last" : "append";
                if(!conversation.isActive() || conversation.hasEnded())
                {
                    conversation.reset();                    
                    command = "first";
                }

                beginConversationIfRequired(requiresConversation);

                // send conversational message
                MessageBuilder.createMessage()
                        .toSubject(conversation.getSubject())
                        .command(command)
                        .with("word", text)
                        .done().sendNowWith(bus);

                toggleFlush(requiresFlush);

               
            }
        });

        conversationScope.add(textBox);

        LayoutPanel inner = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
        inner.add(new Label("Flush?"));
        inner.add(requiresFlush);
        conversationScope.add(inner);
        conversationScope.add(btn2);
        conversationScope.add(requiresConversation);
        conversationScope.add(
                new Button("Terminate Reply", new ClickHandler()
                {
                    public void onClick(ClickEvent clickEvent) {
                        if(conversation!=null)
                        {
                            conversation.end();
                        }
                    }
                }));

        // --------------------------------
        panel.add(requestScope, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));
        panel.add(conversationScope, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

        callback.onSuccess(panel);
    }

    private void toggleFlush(CheckBox requiresFlush) {
        if(conversation.hasEnded() || requiresFlush.getValue())
        {
            requiresFlush.setValue(false);
            conversation.reset();
        }
    }

    private void beginConversationIfRequired(CheckBox requiresConversation) {
        if(requiresConversation.getValue() && !conversation.isActive())
        {
            conversation.begin();
        }
    }
}
