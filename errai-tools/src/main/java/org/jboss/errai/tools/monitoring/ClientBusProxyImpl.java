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

package org.jboss.errai.tools.monitoring;

import com.google.inject.spi.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.MessageListener;
import org.jboss.errai.bus.client.api.SubscribeListener;
import org.jboss.errai.bus.client.api.UnsubscribeListener;
import org.jboss.errai.bus.client.framework.BusMonitor;
import org.jboss.errai.bus.client.framework.MessageBus;

public class ClientBusProxyImpl implements MessageBus {
    private MessageBus serverBus;

    public ClientBusProxyImpl(MessageBus serverBus) {
        this.serverBus = serverBus;
    }

    public void sendGlobal(org.jboss.errai.bus.client.api.Message message) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void send(org.jboss.errai.bus.client.api.Message message) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void send(org.jboss.errai.bus.client.api.Message message, boolean fireListeners) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void conversationWith(org.jboss.errai.bus.client.api.Message message, MessageCallback callback) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void subscribe(String subject, MessageCallback receiver) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void unsubscribeAll(String subject) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isSubscribed(String subject) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addGlobalListener(MessageListener listener) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addSubscribeListener(SubscribeListener listener) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addUnsubscribeListener(UnsubscribeListener listener) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void attachMonitor(BusMonitor monitor) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
