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

package org.errai.samples.helloworld.server;

import com.google.inject.Inject;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.api.base.ConversationMessage;
import org.jboss.errai.bus.client.api.MessageBus;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;

@Service
public class HelloWorldService implements MessageCallback {
    private MessageBus bus;

    @Inject
    public HelloWorldService(MessageBus bus) {
        this.bus = bus;
    }

    public void callback(final CommandMessage message) {
        Thread t = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(50);
                        ConversationMessage.create(message)
                                .toSubject("MessageConsumer")
                                .set("Text", System.currentTimeMillis() + "")
                                .sendNowWith(bus);

                    }
                    catch (InterruptedException e) {
                        return;
                    }

                }
            }
        };

        t.start();


    }
}
