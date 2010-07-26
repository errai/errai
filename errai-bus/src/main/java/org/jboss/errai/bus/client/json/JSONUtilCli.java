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

package org.jboss.errai.bus.client.json;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.framework.MarshalledMessage;
import org.jboss.errai.common.client.json.JSONDecoderCli;
import org.jboss.errai.common.client.json.JSONEncoderCli;

import java.util.ArrayList;
import java.util.Map;

public class JSONUtilCli {
    private static final ArrayList<MarshalledMessage> EMPTYLIST = new ArrayList<MarshalledMessage>(0);

    public static ArrayList<MarshalledMessage> decodePayload(String value) {
        if (value == null || value.trim().length() == 0) return EMPTYLIST;
        ArrayList<MarshalledMessage> list = new ArrayList<MarshalledMessage>();

        /**
         * We have to do a two-stage decoding of the message.  We cannot fully decode the message here, as we
         * cannot be sure the destination endpoint exists within this Errai bundle.  So we extract the ToSubject
         * field and send the unparsed JSON object onwards.
         */
        JSONValue val = JSONParser.parse(value);
        if (val == null) {
            return EMPTYLIST;
        }
        JSONArray arr = val.isArray();
        if (arr == null) {
            throw new RuntimeException("unrecognized payload" + val.toString());
        } 

        for (int i = 0; i < arr.size(); i++) {
            list.add(new MarshalledMessageImpl((JSONObject) arr.get(i)));
        }

        return list;
    }

    public static class MarshalledMessageImpl implements MarshalledMessage {
        public JSONObject o;

        public MarshalledMessageImpl(JSONObject o) {
            this.o = o;
        }

        public Object getMessage() {
            return o;
        }

        public String getSubject() {
            return o.get("ToSubject").isString().stringValue();
        }
    }

    @SuppressWarnings({"unchecked"})
    public static Map<String, Object> decodeMap(Object value) {
        return (Map<String, Object>) new JSONDecoderCli().decode(value);
    }

    public static Message decodeCommandMessage(Object value) {
        return CommandMessage.createWithParts(decodeMap(value));
    }

    public static String encodeMap(Map<String, Object> map) {
        return new JSONEncoderCli().encode(map);
    }
}
