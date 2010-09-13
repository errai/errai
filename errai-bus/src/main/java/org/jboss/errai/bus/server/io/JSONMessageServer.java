/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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
        buf.append('\"').append(a).append('\"').append(':')
                .append(JSONEncoder.encode(b));
    }
}
