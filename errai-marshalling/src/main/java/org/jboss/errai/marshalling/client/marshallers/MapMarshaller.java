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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@ClientMarshaller
@ServerMarshaller
@AlwaysQualify
@ImplementationAliases({AbstractMap.class, HashMap.class})
public class MapMarshaller<T extends Map<Object, Object>> implements Marshaller<T> {
  public static final MapMarshaller INSTANCE = new MapMarshaller();

  @Override
  public Class<T> getTypeHandled() {
    return SimpleTypeLiteral.<T>ofRawType(Map.class).get();
  }

  @SuppressWarnings("unchecked")
  @Override
  public T demarshall(final EJValue o, final MarshallingSession ctx) {
    return doDemarshall((T) new HashMap<Object, Object>(), o, ctx);
  }

  protected T doDemarshall(final T impl,
                                             final EJValue o,
                                             final MarshallingSession ctx) {
    final EJObject jsonObject = o.isObject();

    Object demarshalledKey, demarshalledValue;
    for (final String key : jsonObject.keySet()) {
      if (key.startsWith(SerializationParts.EMBEDDED_JSON)) {
        final EJValue val = ParserFactory.get().parse(key.substring(SerializationParts.EMBEDDED_JSON.length()));
        demarshalledKey = ctx.getMarshallerInstance(ctx.determineTypeFor(null, val)).demarshall(val, ctx);
      }
      else {
        demarshalledKey = key;
      }

      final EJValue v = jsonObject.get(key);
      if (!v.isNull()) {
        demarshalledValue = ctx.getMarshallerInstance(ctx.determineTypeFor(null, v)).demarshall(v, ctx);
      }
      else {
        demarshalledValue = null;
      }
      impl.put(demarshalledKey, demarshalledValue);
    }
    return impl;
  }

  @SuppressWarnings("unchecked")
  @Override
  public String marshall(final T o, final MarshallingSession ctx) {
    final StringBuilder buf = new StringBuilder();
    buf.append("{");
    Object key, val;
    int i = 0;
    for (final Map.Entry<Object, Object> entry : o.entrySet()) {
      if (i++ > 0) {
        buf.append(",");
      }
      key = entry.getKey();
      val = entry.getValue();

      final Marshaller<Object> keyMarshaller;
      final Marshaller<Object> valueMarshaller;
      if (key instanceof String) {
        buf.append("\"").append(key).append("\"");
      }
      else if (key != null) {
        if (key instanceof Number || key instanceof Boolean || key instanceof Character) {
          keyMarshaller = MarshallUtil.getQualifiedNumberMarshaller(key);
        }
        else {
          keyMarshaller = MarshallUtil.getMarshaller(key, ctx);
        }
        buf.append(("\"" + SerializationParts.EMBEDDED_JSON))
                .append(MarshallUtil.jsonStringEscape(keyMarshaller.marshall(key, ctx)))
                .append("\"");
      }

      buf.append(":");

      if (val == null) {
        buf.append("null");
      }
      else {
        if ((val instanceof Number && !(val instanceof BigInteger || val instanceof BigDecimal))
                || val instanceof Boolean || val instanceof Character) {

          valueMarshaller = MarshallUtil.getQualifiedNumberMarshaller(val);
        }
        else {
          valueMarshaller = MarshallUtil.getMarshaller(val, ctx);
        }
        buf.append(valueMarshaller.marshall(val, ctx));
      }
    }

    return buf.append("}").toString();
  }

}