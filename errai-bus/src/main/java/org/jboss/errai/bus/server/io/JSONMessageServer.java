package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.base.JSONMessage;
import org.jboss.errai.bus.client.framework.MessageProvider;

public class JSONMessageServer extends JSONMessage {
    public static final MessageProvider PROVIDER = new MessageProvider() {
        public Message get() {
            return create();
        }
    };

    static JSONMessage create() {
        return new JSONMessageServer();
    }

    @Override
    protected void _addObjectPart(String a, Object b) {
        _sep();
        buf.append(a).append(':')
                .append(JSONEncoder.encode(b));
    }
}
