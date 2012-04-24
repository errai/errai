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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.ParserFactory;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.marshallers.ListMarshaller;
import org.jboss.errai.marshalling.client.marshallers.MapMarshaller;
import org.jboss.errai.marshalling.client.util.NumbersUtils;

/**
 * @author Mike Brock
 */
public abstract class Marshalling {
  public static boolean canHandle(Class<?> type) {
    return MarshallingSessionProviderFactory.getProvider().hasMarshaller(type.getName());
  }

  public static String toJSON(Object obj) {
    if (obj == null) {
      return "{\"" + SerializationParts.ENCODED_TYPE + "\":\"java.lang.Object\",\""
              + SerializationParts.QUALIFIED_VALUE + "\":null}";
    }

    MarshallingSession session = MarshallingSessionProviderFactory.getEncoding();

    if (needsQualification(obj)) {
      return NumbersUtils.qualifiedNumericEncoding(obj);
    }
    else {
      return session.getMarshallerInstance(obj.getClass().getName()).marshall(obj, session);
    }
  }

  public static void toJSON(Appendable appendTo, Object obj) throws IOException {
    appendTo.append(toJSON(obj));
  }

  public static String toJSON(Map<Object, Object> obj) {
    return MapMarshaller.INSTANCE.marshall(obj, MarshallingSessionProviderFactory.getEncoding());
  }

  public static String toJSON(List arr) {
    return ListMarshaller.INSTANCE.marshall(arr, MarshallingSessionProviderFactory.getEncoding());
  }

  public static <T> T fromJSON(String json, Class<T> type) {
    EJValue parsedValue = ParserFactory.get().parse(json);
    MarshallingSession session = MarshallingSessionProviderFactory.getDecoding();
    Marshaller<Object> marshallerInstance = session.getMarshallerInstance(type.getName());
    return (T) marshallerInstance.demarshall(parsedValue, session);
  }

  public static Object fromJSON(String json) {
    return fromJSON(json, Object.class);
  }

  private static boolean needsQualification(Object o) {
    return (o instanceof Number && o.getClass().getName().startsWith("java.lang.") && !(o instanceof Long))
            || o instanceof Boolean || o instanceof Character;
  }
}
