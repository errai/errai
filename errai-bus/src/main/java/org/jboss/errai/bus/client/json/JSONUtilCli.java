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
    public static String MULTI_PAYLOAD_SEPER = "||";
    public static String MULTI_PAYLOAD_SEPER_REGEX = "\\|\\|";

    public static ArrayList<MarshalledMessage> decodePayload(String value) {
        if (value == null || value.trim().length() == 0) return new ArrayList<MarshalledMessage>(0);

        ArrayList<MarshalledMessage> list = new ArrayList<MarshalledMessage>();

        JSONValue a = JSONParser.parse(value);

        if (a instanceof JSONArray) {
            JSONArray arr = (JSONArray) a;

            for (int i = 0; i < arr.size(); i++) {
                if ((a = arr.get(i)) instanceof JSONObject) {
                    final JSONObject eMap = (JSONObject) a;
                    final String subject = eMap.keySet().iterator().next();

                    list.add(new MarshalledMessage() {
                        public String getSubject() {
                            return subject;
                        }

                        public Object getMessage() {
                            return eMap.get(subject);
                        }
                    });
                }
            }
        }

        return list;
    }

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
