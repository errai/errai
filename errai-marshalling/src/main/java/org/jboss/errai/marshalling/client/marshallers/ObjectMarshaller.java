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
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.annotations.ServerMarshaller;
import org.jboss.errai.marshalling.client.api.json.EJObject;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.util.MarshallUtil;
import org.jboss.errai.marshalling.client.util.NumbersUtils;

/**
 * This class is used to handle untyped Objects on the wire.
 *
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@ClientMarshaller
@ServerMarshaller
public class ObjectMarshaller extends AbstractNullableMarshaller<Object> {
  @Override
  public Class<Object> getTypeHandled() {
    return Object.class;
  }

  @Override
  public Object[] getEmptyArray() {
    return new Object[0];
  }

  public final Object demarshall(final Class<?> targetType, final EJValue o, final MarshallingSession ctx) {
    if (o.isNull()) {
      return null;
    }

    if (o.isObject() != null) {
      final EJObject jsObject = o.isObject();
      final EJValue ejEncType = jsObject.get(SerializationParts.ENCODED_TYPE);
      String encodedType = null;
      if (!ejEncType.isNull() && ejEncType.isString() != null) {
        encodedType = ejEncType.isString().stringValue();
      }

      if (encodedType == null) {
        if (targetType == null) {
          return MapMarshaller.INSTANCE.demarshall(o, ctx);
        }
        else if (ctx.getMarshallerInstance(targetType.getName()) != null) {
          return ctx.getMarshallerInstance(targetType.getName()).demarshall(o, ctx);
        }
      }

      if (jsObject.containsKey(SerializationParts.NUMERIC_VALUE)) {
        return NumbersUtils.getNumber(encodedType, jsObject.get(SerializationParts.NUMERIC_VALUE));
      }

      if (ctx.getMarshallerInstance(encodedType) == null) {
        throw new RuntimeException("marshalled type is unknown to the marshalling framework: " + encodedType);
      }

      return ctx.getMarshallerInstance(encodedType).demarshall(o, ctx);
    }
    else if (o.isArray() != null) {
      return new ListMarshaller().demarshall(o, ctx);
    }
    else if (o.isString() != null) {
      return o.isString().stringValue();
    }
    else if (o.isBoolean() !=null ) {
    	return o.isBoolean().booleanValue();
    }
    else if (o.isNumber() != null ) {
    	return o.isNumber().doubleValue();
    }

    return null;
  }

  @Override
  public Object doNotNullDemarshall(final EJValue o, final MarshallingSession ctx) {
    return this.demarshall(null, o, ctx);
  }

  @Override
  public String doNotNullMarshall(final Object o, final MarshallingSession ctx) {
    if ((o instanceof Number && !o.getClass().getName().startsWith("java.math.Big")) || o instanceof Boolean) {
      return NumbersUtils.qualifiedNumericEncoding(o);
    }

    if (MarshallUtil.getMarshaller(o, ctx) == null) {
      throw new RuntimeException("marshalled type is unknown to the marshalling framework: " + o.getClass().getName());
    }

    return MarshallUtil.getMarshaller(o, ctx).marshall(o, ctx);
  }

}
