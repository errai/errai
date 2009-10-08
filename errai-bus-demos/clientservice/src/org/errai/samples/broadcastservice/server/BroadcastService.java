package org.errai.samples.broadcastservice.server;

import com.google.inject.Inject;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.MessageCallback;
import org.jboss.errai.bus.server.annotations.Service;

@Service
public class BroadcastService implements MessageCallback {
    private MessageBus bus;

    @Inject
    public BroadcastService(MessageBus bus) {
        this.bus = bus;
    }

    public void callback(CommandMessage message) {
        CommandMessage.create()
                .toSubject("BroadcastReceiver")
                .copy("BroadcastText", message)
                .sendNowWith(bus);
    }
}
