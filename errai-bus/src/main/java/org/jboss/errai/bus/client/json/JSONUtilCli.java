/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.Message;
import org.jboss.errai.common.client.json.JSONDecoderCli;
import org.jboss.errai.common.client.json.JSONEncoderCli;

import java.util.ArrayList;
import java.util.Map;

public class JSONUtilCli {
    public static ArrayList<Message> decodePayload(Object value) {
        String str = String.valueOf(value);

        if (value == null || str.trim().length() == 0) return new ArrayList<Message>(0);

        ArrayList<Message> list = new ArrayList<Message>();
        JSONValue a = JSONParser.parse(str);

        if (a instanceof JSONArray) {
            JSONArray arr = (JSONArray) a;

            for (int i = 0; i < arr.size(); i++) {
                a = arr.get(i);

                if (a instanceof JSONObject) {
                    final JSONObject eMap = (JSONObject) a;
                    final String subject = eMap.keySet().iterator().next();

                    list.add(new Message() {
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

    public static CommandMessage decodeCommandMessage(Object value) {
        return new CommandMessage(decodeMap(value));
    }

    public static String encodeMap(Map<String, Object> map) {
        return new JSONEncoderCli().encode(map);
    }
}
