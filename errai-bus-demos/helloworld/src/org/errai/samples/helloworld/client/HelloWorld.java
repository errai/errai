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

package org.errai.samples.helloworld.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.errai.bus.client.*;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.api.base.ConversationMessage;

public class HelloWorld implements EntryPoint {
    private MessageBus bus = ErraiBus.get();

    public void onModuleLoad() {
        final HTML html = new HTML();

        bus.subscribe("MessageConsumer",
                new MessageCallback() {
                    public void callback(CommandMessage message) {
                        html.setHTML(message.get(String.class, "Text"));
                    }
                });

        ConversationMessage.create()
                .toSubject("HelloWorldService")
                .sendNowWith(bus);

        RootPanel.get().add(html);

    }
}
