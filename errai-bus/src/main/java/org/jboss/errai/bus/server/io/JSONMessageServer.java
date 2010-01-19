package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.JSONMessage;

public class JSONMessageServer extends JSONMessage {
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
