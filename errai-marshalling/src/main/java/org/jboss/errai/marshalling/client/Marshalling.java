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

package org.jboss.errai.marshalling.client;

import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.ParserFactory;

import java.io.IOException;

/**
 * @author Mike Brock
 */
public abstract class Marshalling {
  public static boolean canHandle(Class<?> type) {
    return MarshallingSessionProviderFactory.getProvider().hasMarshaller(type.getName());
  }

  public static String toJSON(Object obj) {
    if (obj == null) {
      return "null";
    }

    MarshallingSession session = MarshallingSessionProviderFactory.getEncoding();
    return session.getMarshallerInstance(obj.getClass().getName()).marshall(obj, session);
  }

  public static void toJSON(Appendable appendTo, Object obj) throws IOException {
    appendTo.append(toJSON(obj));
  }

  public static <T> T fromJSON(String json, Class<T> type) {
    MarshallingSession session = MarshallingSessionProviderFactory.getDecoding();
    return (T) session.getMarshallerInstance(type.getName()).demarshall(ParserFactory.get().parse(json), session);
  }

  public static Object fromJSON(String json) {
    return fromJSON(json, Object.class);
  }
}
