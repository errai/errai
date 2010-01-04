package org.jboss.errai.bus.client;

import org.jboss.errai.bus.client.protocols.MessageParts;

public class RemoteCall {
    private static int callCounter = 0;

    /**
     * Create a new CommandMessage.
     *
     * @return a new instance of CommandMessage
     */
    public static RemoteCall create() {
        return new RemoteCall();
    }

    public RemoteCallEndpoint call(final String service) {
        return new RemoteCallEndpoint() {

            public RemoteCallRespondDef endpoint(final String name) {
                return new AbstractCallRespondDef() {
                    public void sendNowWith(MessageBus bus) {
                        endpoint(name, null).respondTo(remoteCallback).sendNowWith(bus);
                    }
                };
            }

            public RemoteCallRespondDef endpoint(final String name, final Object... args) {
                return new AbstractCallRespondDef() {

                    public void sendNowWith(final MessageBus bus) {
                        Message msg = CommandMessage.create()
                                                     .toSubject(service)
                                                     .command(name);

                        if (remoteCallback != null) {
                            final String replyTo = service + "." + name + ":RespondTo:" + (++callCounter);


                            if (args != null) {
                                msg.set("MethodParms", args);
                            }
                            if (remoteCallback != null) {
                                bus.subscribe(replyTo,
                                        new MessageCallback() {
                                            public void callback(Message message) {
                                                bus.unsubscribeAll(replyTo);
                                                remoteCallback.callback(message.get(responseType, "MethodReply"));
                                            }
                                        });
                                msg.set(MessageParts.ReplyTo, replyTo);
                            }
                        }
                        msg.sendNowWith(bus);
                    }
                };
            }
        };
    }


    public interface RemoteCallEndpoint {
        public RemoteCallRespondDef endpoint(String name);

        public RemoteCallRespondDef endpoint(String name, Object... args);
    }

    public interface RemoteCallSendable {
        public void sendNowWith(MessageBus bus);
    }

    public interface RemoteCallRespondDef extends RemoteCallSendable {
        public RemoteCallSendable respondTo(RemoteCallback callback);
        public RemoteCallSendable respondTo(Class responseType, RemoteCallback callback);
    }

    public abstract class AbstractCallRespondDef implements RemoteCallRespondDef {
        protected Class responseType;
        protected RemoteCallback remoteCallback;

        public RemoteCallSendable respondTo(RemoteCallback callback) {
            this.remoteCallback = callback;
            return this;
        }

        public RemoteCallSendable respondTo(Class responseType, RemoteCallback callback) {
            this.responseType = responseType;
            this.remoteCallback = callback;
            return this;
        }
    }

}
