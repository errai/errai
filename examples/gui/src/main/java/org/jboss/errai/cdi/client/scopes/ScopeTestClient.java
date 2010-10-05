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
import com.google.gwt.user.client.ui.*;
import org.gwt.mosaic.ui.client.CaptionLayoutPanel;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.builder.MessageBuildParms;
import org.jboss.errai.bus.client.api.builder.MessageBuildSendableWithReply;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.cdi.client.CDI;
import org.jboss.errai.cdi.client.api.Conversation;
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

    private boolean runningConversation = false;

    private String conversationId;

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

        CaptionLayoutPanel conversationScope = new CaptionLayoutPanel("Conversation Scope Beans");
        conversationScope.setLayout(new BoxLayout(BoxLayout.Orientation.VERTICAL));

        final TextBox textBox = new TextBox();
        final CheckBox flush = new CheckBox();
        final CheckBox isConversational = new CheckBox();
        isConversational.setValue(true);

        Button btn2 = new Button("Append to buffer", new ClickHandler()
        {
            public void onClick(ClickEvent clickEvent) {
                String text = textBox.getText();
                textBox.setText("");

                String command = flush.getValue() ? "last" : "append";
                if(!runningConversation)
                {
                    command = "first";
                    runningConversation = true;
                    conversationId = CDI.generateId();
                }

                if(flush.getValue())
                {
                    runningConversation = false;                    
                }

                flush.setValue(false);

                MessageBuildParms<MessageBuildSendableWithReply> parms = MessageBuilder.createMessage()
                        .toSubject("wizard")
                        .command(command)
                        .with("word", text);

                if(isConversational.getValue())
                {
                    parms.with("conversationId", conversationId);
                }

                parms.done().sendNowWith(bus);

            }
        });

        conversationScope.add(textBox);

        LayoutPanel inner = new LayoutPanel(new BoxLayout(BoxLayout.Orientation.VERTICAL));
        inner.add(new Label("Flush?"));
        inner.add(flush);
        conversationScope.add(inner);
        conversationScope.add(btn2);
        conversationScope.add(isConversational);

        // --------------------------------
        panel.add(requestScope, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));
        panel.add(conversationScope, new BoxLayoutData(BoxLayoutData.FillStyle.BOTH));

        callback.onSuccess(panel);
    }
}
