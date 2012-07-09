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
import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.json.EJObject;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.util.MarshallUtil;
import org.jboss.errai.marshalling.client.util.SimpleTypeLiteral;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ErraiProtocolEnvelopeMarshaller implements Marshaller<Map<String, Object>> {
  public static final ErraiProtocolEnvelopeMarshaller INSTANCE = new ErraiProtocolEnvelopeMarshaller();

  @Override
  public Class<Map<String, Object>> getTypeHandled() {
    return SimpleTypeLiteral.<Map<String, Object>>ofRawType(Map.class).get();
  }

  @Override
  public Map<String, Object> demarshall(EJValue o, MarshallingSession ctx) {
    return doDermashall(new HashMap(), o, ctx);
  }

  protected Map doDermashall(Map impl, EJValue o, MarshallingSession ctx) {
    EJObject jsonObject = o.isObject();

    for (String key : jsonObject.keySet()) {
      if (MessageParts.SessionID.name().equals(key)) continue;
      EJValue v = jsonObject.get(key);
      if (!v.isNull()) {
        impl.put(key, ctx.getMarshallerInstance(ctx.determineTypeFor(null, v)).demarshall(v, ctx));
      }
      else {
        impl.put(key, null);
      }
    }
    return impl;
  }

  @Override
  public String marshall(Map<String, Object> o, MarshallingSession ctx) {
    StringBuilder buf = new StringBuilder();

    buf.append("{");
    Object key, val;
    int i = 0;
    for (Map.Entry<String, Object> entry : o.entrySet()) {
      key = entry.getKey();
      val = entry.getValue();

      if (MessageParts.SessionID.name().equals(key)) continue;

      if (i++ > 0) {
        buf.append(",");
      }


      Marshaller<Object> valueMarshaller;
      buf.append("\"" + key + "\"").append(":");

      if (val == null) {
        buf.append("null");
      }
      else {
        if ((val instanceof Number && !(val instanceof BigInteger || val instanceof BigDecimal))
                || val instanceof Boolean || val instanceof Character) {

          valueMarshaller = MarshallUtil.getQualifiedNumberMarshaller(val);
        }
        else {
          valueMarshaller = ctx.getMarshallerInstance(val.getClass().getName());
        }
        buf.append(valueMarshaller.marshall(val, ctx));
      }
    }

    return buf.append("}").toString();
  }

  @Override
  public Map<String, Object>[] getEmptyArray() {
    throw new UnsupportedOperationException("Not implemented!");
  }


}