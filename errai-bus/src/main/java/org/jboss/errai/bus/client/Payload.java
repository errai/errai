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

package org.jboss.errai.bus.client;

import java.util.LinkedList;
import java.util.List;

/**
 * The <tt>Payload</tt> class represents one-or-more messages to be transmitted from one bus to another.
 * <p/>
 * The payload may contain multiple messages for transmission in a single communication event across buses.  Bus
 * implementations will automatically group messages within transmission windows to improve network efficiency.
 * <p/>
 * Messages are always transmitted in the order the arrive for transmission.
 */
public class Payload {
    private List<Message> messages = new LinkedList<Message>();

    /**
     * Creates a new <tt>Payload</tt> with a single initial {@link org.jboss.errai.bus.client.Message}.
     * @param m a message
     */
    public Payload(Message m) {
        messages.add(m);
    }

    /**
     * Adds a new message to the <tt>Payload</tt>.
     * @param m
     */
    public void addMessage(Message m) {
        messages.add(m);
    }

    /**
     * Return all messages within the <tt>Payload</tt>
     * @return a list of all messages in the payload.
     */
    public List<Message> getMessages() {
        return messages;
    }

    public boolean waitingMessages() {
        return messages.size() > 1 ||!messages.get(0).getSubject().equals("HeartBeat");
    }
}
