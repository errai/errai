package org.jboss.errai.bus.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.RequestDispatcher;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.util.ErrorHelper;

import static org.jboss.errai.bus.server.util.ErrorHelper.handleMessageDeliveryFailure;

/**
 * Simple request dispatcher implementation.
 *
 * @see org.jboss.errai.bus.server.AsyncDispatcher
 */
@Singleton
public class SimpleDispatcher implements RequestDispatcher {
    private ErraiService svc;
    private MessageBus bus;

    @Inject
    public SimpleDispatcher(ErraiService svc) {
        this.svc = svc;
        this.bus = svc.getBus();
    }

    public void dispatchGlobal(Message message) {
        try {
            bus.sendGlobal(message);
        }
        catch (QueueUnavailableException e) {
            handleMessageDeliveryFailure(bus, message, "Queue is not available", e, true);
        }
        catch (Exception e) {
            handleMessageDeliveryFailure(bus, message, "Error calling remote service: " + message.getSubject(), e, false);
        }
    }

    public void dispatch(Message message) {
        try {
            bus.send(message);
        }
        catch (QueueUnavailableException e) {
            handleMessageDeliveryFailure(bus, message, "Queue is not available", e, true);
        }
        catch (Exception e) {
            handleMessageDeliveryFailure(bus, message, "Error calling remote service: " + message.getSubject(), e, false);
        }
    }
}
