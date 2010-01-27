package org.jboss.errai.bus.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jboss.errai.bus.client.ErrorCallback;
import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.client.RequestDispatcher;
import org.jboss.errai.bus.server.WorkerFactory;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.server.service.ErraiService;

/**
 * The <tt>AsyncDispatcher</tt> provides asynchronous message delivery into the bus.  This means that incoming remote
 * requests do not block, and processing of the request continues even after the incoming network conversation has
 * ended.
 * </p>
 * This dispatcher implementation can be used with the {@link org.jboss.errai.bus.server.servlet.DefaultBlockingServlet}
 * as this pertains to incoming--as opposed to outgoing--message handling. Note: some appservers or servlet environments
 * may restrict thread creation within the container, in which case this implementation cannot be used.
 */
@Singleton
public class AsyncDispatcher implements RequestDispatcher {
    private WorkerFactory workerFactory;
    private ErraiService service;

    /**
     * Constructs the <tt>AsyncDispatcher</tt> with the specified service. The injection makes it possible to obtain
     * a reference to the <tt>ErraiService</tt>
     *
     * @param service - the service where the bus is located
     */
    @Inject
    public AsyncDispatcher(ErraiService service) {
        this.service = service;
        this.workerFactory = new WorkerFactory(service);
    }

    /**
     * Sends the message globally. If the <tt>PriorityProcessing</tt> routing flag is set, then the message is sent
     * globally on the bus. If not, the message is sent globally through the <tt>workerFactory</tt>
     *
     * @param message - a message to dispatch globally
     */
    public void dispatchGlobal(Message message) {
        if (message.hasPart(MessageParts.PriorityProcessing)) {
            try {
            service.getBus().sendGlobal(message);
            }
            catch (Throwable t) {
                if (message.getErrorCallback() != null) {
                    if (!message.getErrorCallback().error(message, t)) {
                        return;
                    }
                }
            }
        } else {
            workerFactory.deliverGlobal(message);
        }
    }

    /**
     *
     * @param message - a message to dispatch
     */
    public void dispatch(Message message) {
         workerFactory.deliver(message);
    }
}
