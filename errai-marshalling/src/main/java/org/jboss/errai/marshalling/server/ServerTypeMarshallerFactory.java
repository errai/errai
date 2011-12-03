/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.marshalling.server;

import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.annotations.ServerMarshaller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class ServerTypeMarshallerFactory {
  private static final Map<String, Marshaller<Object, Object>> MARSHALLERS
          = new HashMap<String, Marshaller<Object, Object>>();

  static {
    loadMarshallers();
  }

  public static boolean hasMarshaller(Class<?> cls) {
    return hasMarshaller(cls.getName());
  }
  
  public static boolean hasMarshaller(String str) {
    return MARSHALLERS.containsKey(str);
  }

  public static Marshaller<Object, Object> getMarshaller(Class<?> cls) {
    return getMarshaller(cls.getName());
  }
  
  public static Marshaller<Object, Object> getMarshaller(String str) {
    return MARSHALLERS.get(str);
  }

  public static void registerMarshaller(String str, Marshaller<Object, Object> marshaller) {
    MARSHALLERS.put(str, marshaller);
  }

  private static void loadMarshallers() {
    Set<Class<?>> marshallers =
            ScannerSingleton.getOrCreateInstance().getTypesAnnotatedWith(ServerMarshaller.class);

    for (Class<?> m : marshallers) {
      try {
        Marshaller<Object, Object> marshaller = (Marshaller<Object, Object>) m.newInstance();

        MARSHALLERS.put(marshaller.getTypeHandled().getName(), marshaller);
      }
      catch (ClassCastException e) {
        throw new RuntimeException("@ServerMarshaller class "
                + m.getName() + " is not an instance of " + Marshaller.class.getName());
      }
      catch (Throwable t) {
        throw new RuntimeException("Error instantiating " + m.getName(), t);
      }
    }
  }

}
