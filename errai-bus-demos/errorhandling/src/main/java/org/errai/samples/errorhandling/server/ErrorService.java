package org.errai.samples.errorhandling.server;

import com.google.inject.Inject;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.annotations.Service;

@Service
public class ErrorService implements MessageCallback {
    private MessageBus bus;

    @Inject
    public ErrorService(MessageBus bus) {
        this.bus = bus;
    }

    public void callback(Message message) {

    }
}
