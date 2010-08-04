/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.bus.client.tests;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.client.tests.support.SType;

/**
 * User: christopherbrock
 * Date: 3-Aug-2010
 * Time: 6:57:08 PM
 */
public class BusSerializationTest extends AbstractErraiTest {

    @Override
    public String getModuleName() {
        return "org.jboss.errai.bus.ErraiBusTests";
    }

    public void testSerializableCase() {
        
        runAfterInit(new Runnable() {
            public void run() {
                final SType sType1 = SType.create();

                bus.subscribe("ClientReceiver", new MessageCallback() {
                    public void callback(Message message) {
                        SType type = message.get(SType.class, "SType");

                        try {
                            assertNotNull(type);
                            assertTrue(sType1.equals(type));
                            System.out.println("CLIENT: " + type.toString());
                            finishTest();
                            return;
                        }
                        catch (Exception e) {
                            e.printStackTrace();

                        }
                        fail();
                    }
                });

                MessageBuilder.createMessage()
                        .toSubject("TestService1")
                        .with("SType", sType1)
                        .with(MessageParts.ReplyTo, "ClientReceiver")
                        .done().sendNowWith(bus);
            }
        });
    }
}