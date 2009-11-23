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

package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.CommandMessage;
import org.mvel2.ConversionHandler;
import org.mvel2.DataConversion;
import org.mvel2.MVEL;

import javax.swing.text.html.HTMLDocument;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import static org.mvel2.DataConversion.addConversionHandler;

public class TypeDemarshallHelper {
    static {
        addConversionHandler(java.sql.Date.class, new ConversionHandler() {
            public Object convertFrom(Object o) {
                return new java.sql.Date(((Number) o).longValue());
            }

            public boolean canConvertFrom(Class aClass) {
                return Number.class.isAssignableFrom(aClass);
            }
        });

        addConversionHandler(java.util.Date.class, new ConversionHandler() {
            public Object convertFrom(Object o) {
                return new java.util.Date(((Number) o).longValue());
            }

            public boolean canConvertFrom(Class aClass) {
                return Number.class.isAssignableFrom(aClass);
            }
        });
    }

    public static void demarshallAll(String object, CommandMessage command) {
        try {
            for (String t : object.split(",")) {
                command.set(t, _demarshallAll(command.get(Object.class, t)));
            }
        }
        catch (Exception e) {
            throw new RuntimeException("could not demarshall types for message parts:" + object, e);
        }
    }

    public static Object _demarshallAll(Object o) throws Exception {
        try {
            if (o instanceof String) {
                return o;

            } else if (o instanceof Collection) {
                ArrayList newList = new ArrayList(((Collection) o).size());
                for (Object o2 : ((Collection) o)) {
                    newList.add(_demarshallAll(o2));
                }
                return newList;
            } else if (o instanceof Map) {
                Map<?, ?> oMap = (Map) o;
                if (oMap.containsKey("__EncodedType")) {
                    Object newInstance = Class.forName((String) oMap.get("__EncodedType")).newInstance();

                    for (Map.Entry<?, ?> entry : oMap.entrySet()) {
                        if ("__EncodedType".equals(entry.getKey())) continue;
                        MVEL.setProperty(newInstance, (String) entry.getKey(), _demarshallAll(entry.getValue()));
                    }

                    return newInstance;
                }
            }
            return o;
        }
        catch (Exception e) {
            throw new RuntimeException("error demarshalling encoded object:\n" + o, e);
        }
    }
}
