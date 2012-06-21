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
import org.jboss.errai.common.client.util.Base64Util;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.json.EJObject;
import org.jboss.errai.marshalling.client.api.json.EJValue;

/**
 * Used to wrap marshallers annotated with {@link org.jboss.errai.marshalling.client.api.annotations.AlwaysQualify}
 * 
 * @author Mike Brock
 */
public class QualifyingMarshallerWrapper<T> extends AbstractNullableMarshaller<T> {
  private final Marshaller<T> delegate;

  public QualifyingMarshallerWrapper(Marshaller<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public Class<T> getTypeHandled() {
    return delegate.getTypeHandled();
  }

  @Override
  public T doNotNullDemarshall(EJValue o, MarshallingSession ctx) {
    EJObject obj = o.isObject();

    T val = null;
    if (obj != null) {
      String objId = obj.get(SerializationParts.OBJECT_ID).isString().stringValue();
      if (ctx.hasObjectHash(objId)) {
        // noinspection unchecked
        return (T) ctx.getObject(Object.class, objId);
      }

      val = delegate.demarshall(o.isObject().get(SerializationParts.QUALIFIED_VALUE), ctx);
      ctx.recordObjectHash(objId, val);
    }
    else {
      // This is only to support Jackson's char[] and byte[] representations
      if (o.isString() != null) {
        if (getTypeHandled().equals(byte.class)) {
          val = (T) Base64Util.decode(o.isString().stringValue());
        }
        else if (getTypeHandled().equals(char.class)) {
          val = (T) o.isString().stringValue().toCharArray();
        }
      }
    }
    return val;
  }

  @Override
  public String doNotNullMarshall(T o, MarshallingSession ctx) {
    final boolean isNew = !ctx.hasObjectHash(o);
    final String objId = ctx.getObjectHash(o);

    final StringBuilder buf = new StringBuilder("{\"").append(SerializationParts.ENCODED_TYPE).append("\":\"")
            .append(o.getClass().getName()).append("\",\"").append(SerializationParts.OBJECT_ID).append("\":\"")
            .append(objId).append("\"");

    if (!isNew) {
      return buf.append("}").toString();
    }
    else {
      return buf.append(",\"").append(SerializationParts.QUALIFIED_VALUE).append("\":").append(
          delegate.marshall(o, ctx))
              .append("}").toString();
    }
  }
}
