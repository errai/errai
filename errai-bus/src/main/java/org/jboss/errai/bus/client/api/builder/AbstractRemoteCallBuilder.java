package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.api.*;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RemoteProxyFactory;
import org.jboss.errai.bus.client.protocols.MessageParts;

/**
 * The <tt>AbstractRemoteCallBuilder</tt> facilitates the building of a remote call. Ensures that the remote call is
 * constructed properly
 */
public class AbstractRemoteCallBuilder {

    /* Used to generate a unique number */
    private volatile static int callCounter = 0;

    private final Message message;
    private RemoteCallback remoteCallback;

    /* The type of response that is expected by the callback */
    private Class<?> responseType = Object.class;

    public AbstractRemoteCallBuilder(Message message) {
        this.message = message;
    }

    public <T> T call(final Class<T> remoteService) {
        return RemoteProxyFactory.getRemoteProxy(remoteService);
    }


    /**
     * Creates, implements and returns an instance of <tt>RemoteCallEndpointDef</tt> and all applicable arguments,
     * which should be instantiated after this call to <tt>serviceName</tt>. The endpoint allows a function from a
     * service to be called directly, rather than waiting for a response to a message.
     *
     * @param serviceName - the service to call, and create a remote call endpoint for
     * @return the remote call endpoint created
     */
    public RemoteCallEndpointDef call(final String serviceName) {
        message.toSubject(serviceName + ":RPC");

        final RemoteCallSendable sendable = new RemoteCallSendable() {
            public void sendNowWith(final MessageBus bus) {
                if (remoteCallback != null) {
                    final String replyTo = message.getSubject() + "." + message.getCommandType() + ":RespondTo:" + uniqueNumber();

                    if (remoteCallback != null) {
                        bus.subscribe(replyTo,
                                new MessageCallback() {
                                    public void callback(Message message) {
                                        bus.unsubscribeAll(replyTo);
                                        remoteCallback.callback(message.get(responseType, "MethodReply"));
                                    }
                                });
                        message.set(MessageParts.ReplyTo, replyTo);
                    }
                }
                message.sendNowWith(bus);
            }
        };

        final RemoteCallErrorDef errorDef = new RemoteCallErrorDef() {
            public RemoteCallSendable errorsHandledBy(ErrorCallback errorCallback) {
                message.errorsCall(errorCallback);
                return sendable;
            }

            public RemoteCallSendable noErrorHandling() {
                return sendable;
            }
        };

        final RemoteCallResponseDef respondDef = new RemoteCallResponseDef() {
            public RemoteCallErrorDef respondTo(RemoteCallback callback) {
                remoteCallback = callback;
                return errorDef;
            }

            public <T> RemoteCallErrorDef respondTo(Class<T> returnType, RemoteCallback<T> callback) {
                responseType = returnType;
                remoteCallback = callback;
                return errorDef;
            }
        };

        return new RemoteCallEndpointDef() {
            public RemoteCallResponseDef endpoint(String endPointName) {
                message.command(endPointName);
                return respondDef;
            }

            public RemoteCallResponseDef endpoint(String endPointName, Object... args) {
                message.command(endPointName);
                if (args != null) message.set("MethodParms", args);
                return respondDef;
            }
        };

    }
    
    private static int uniqueNumber() {
        return ++callCounter > 10000 ? callCounter = 0 : callCounter;
    }
}