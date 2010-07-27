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

import com.google.gwt.junit.client.GWTTestCase;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.ClientMessageBus;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.bus.client.framework.LogAdapter;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.client.tests.support.SType;

import java.util.Date;

/**
 * User: christopherbrock
 * Date: 26-Jul-2010
 * Time: 3:21:22 PM
 */
public class BusCommunicationTests extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "org.jboss.errai.bus.ErraiBusTests";
    }

    ClientMessageBus bus;

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();

        bus = (ClientMessageBusImpl) ErraiBus.get();
        bus.setLogAdapter(new LogAdapter() {
            public void warn(String message) {
                System.out.println("WARN: " + message);
            }

            public void info(String message) {
                System.out.println("INFO: " + message);
            }

            public void debug(String message) {
                System.out.println("DEBUG: " + message);
            }

            public void error(String message, Throwable t) {
                System.out.println("ERROR: " + message);
                if (t != null) t.printStackTrace();
            }
        });
    }

    public void testBasicRoundTrip() {
        bus.subscribe("MyTestService", new MessageCallback() {
            public void callback(Message message) {
                System.out.println("GOT ECHO");
                finishTest();
            }
        });

        MessageBuilder.createMessage()
                .toSubject("ServerEchoService")
                .with(MessageParts.ReplyTo, "MyTestService")
                .done().sendNowWith(bus);

        delayTestFinish(5000);
    }


    public void testSerializableCase() {
        bus.addPostInitTask(new Runnable() {
            public void run() {
                final SType sType1 = new SType();
                sType1.setActive(true);
                sType1.setEndDate(new Date(System.currentTimeMillis()));
                sType1.setStartDate(new Date(System.currentTimeMillis() - 1000000));
                sType1.setFieldOne("One!");
                sType1.setFieldTwo("Two!!");
                bus.subscribe("ClientReceiver", new MessageCallback() {
                    public void callback(Message message) {
                        SType type = message.get(SType.class, "SType");

                        assertNotNull(type);
                        assertEquals(sType1.toString(), type.toString());
                        System.out.println("CLIENT: " + type.toString());
                        finishTest();
                    }
                });

                MessageBuilder.createMessage()
                        .toSubject("TestService1")
                        .with("SType", sType1)
                        .with(MessageParts.ReplyTo, "ClientReceiver")
                        .done().sendNowWith(bus);
            }
        });

        delayTestFinish(5000);
    }


}
