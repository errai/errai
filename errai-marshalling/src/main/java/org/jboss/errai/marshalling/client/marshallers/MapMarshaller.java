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

package org.jboss.errai.marshalling.client.marshallers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.ParserFactory;
import org.jboss.errai.marshalling.client.api.annotations.AlwaysQualify;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.annotations.ImplementationAliases;
import org.jboss.errai.marshalling.client.api.annotations.ServerMarshaller;
import org.jboss.errai.marshalling.client.api.json.EJObject;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.util.MarshallUtil;
import org.jboss.errai.marshalling.client.util.SimpleTypeLiteral;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@ClientMarshaller
@ServerMarshaller
@AlwaysQualify
@ImplementationAliases({ AbstractMap.class, HashMap.class })
public class MapMarshaller<T extends Map<Object, Object>> implements Marshaller<T> {
  public static final MapMarshaller INSTANCE = new MapMarshaller();
  private static final HashMap[] EMPTY_ARRAY = new HashMap[0];

  @Override
  public Class<T> getTypeHandled() {
    return SimpleTypeLiteral.<T> ofRawType(Map.class).get();
  }

  @Override
  public T[] getEmptyArray() {
    return (T[]) EMPTY_ARRAY;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T demarshall(final EJValue o, final MarshallingSession ctx) {
    return doDemarshall((T) new HashMap(), o, ctx);
  }

  protected T doDemarshall(final T impl, final EJValue o, final MarshallingSession ctx) {
    Object demarshalledKey;
    for (final String key : o.isObject().keySet()) {
      if (key.startsWith(SerializationParts.EMBEDDED_JSON)) {
        final EJValue val = ParserFactory.get().parse(key.substring(SerializationParts.EMBEDDED_JSON.length()));
        demarshalledKey = ctx.getMarshallerInstance(ctx.determineTypeFor(null, val)).demarshall(val, ctx);
      }
      else {
        demarshalledKey = key;
      }

      final EJValue v = o.isObject().get(key);
      impl.put(demarshalledKey, ctx.getMarshallerInstance(ctx.determineTypeFor(null, v)).demarshall(v, ctx));
    }
    return impl;
  }

  @SuppressWarnings("unchecked")
  @Override
  public String marshall(final T o, final MarshallingSession ctx) {
    final StringBuilder buf = new StringBuilder();
    buf.append("{");
    int i = 0;
    for (final Map.Entry<Object, Object> entry : o.entrySet()) {
      if (i++ > 0) {
        buf.append(",");
      }

      final Marshaller<Object> keyMarshaller;
      final Marshaller<Object> valueMarshaller;
      if (entry.getKey() instanceof String) {
        buf.append("\"").append(entry.getKey()).append("\"");
      }
      else if (entry.getKey() != null) {
        if (entry.getKey() instanceof Number || entry.getKey() instanceof Boolean || entry.getKey() instanceof Character) {
          keyMarshaller = MarshallUtil.getQualifiedNumberMarshaller(entry.getKey());
        }
        else {
          keyMarshaller = MarshallUtil.getMarshaller(entry.getKey(), ctx);
        }
        buf.append(("\"" + SerializationParts.EMBEDDED_JSON))
                .append(MarshallUtil.jsonStringEscape(keyMarshaller.marshall(entry.getKey(), ctx)))
                .append("\"");
      }

      buf.append(":");

      if (entry.getValue() == null) {
        buf.append("null");
      }
      else {
        if ((entry.getValue() instanceof Number && !(entry.getValue() instanceof BigInteger || entry.getValue() instanceof BigDecimal))
                || entry.getValue() instanceof Boolean || entry.getValue() instanceof Character) {

          valueMarshaller = MarshallUtil.getQualifiedNumberMarshaller(entry.getValue());
        }
        else {
          valueMarshaller = MarshallUtil.getMarshaller(entry.getValue(), ctx);
        }
        buf.append(valueMarshaller.marshall(entry.getValue(), ctx));
      }
    }

    return buf.append("}").toString();
  }
}