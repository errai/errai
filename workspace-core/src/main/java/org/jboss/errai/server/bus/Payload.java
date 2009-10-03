package org.jboss.errai.server.bus;

import org.jboss.errai.client.rpc.Message;

import java.util.LinkedList;
import java.util.List;

public class Payload {
    private List<Message> messages = new LinkedList<Message>();

    public Payload(Message m) {
        messages.add(m);
    }

    public void addMessage(Message m) {
        messages.add(m);
    }

    public List<Message> getMessages() {
        return messages;
    }
}
