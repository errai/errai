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

import com.google.gwt.json.client.JSONValue;
import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller
public class ByteMarshaller implements Marshaller<JSONValue, Byte> {
  @Override
  public Class<Byte> getTypeHandled() {
    return Byte.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public Byte demarshall(JSONValue o, MarshallingSession ctx) {
    if (o == null) {
      return null;
    }
    else if (o.isObject() != null) {
      return new Double(o.isObject().get(SerializationParts.NUMERIC_VALUE).isNumber().doubleValue()).byteValue();
    }
    else {
      return new Double(o.isNumber().doubleValue()).byteValue();
    }
  }

  @Override
  public String marshall(Byte o, MarshallingSession ctx) {
    return String.valueOf(o.intValue());
  }

  @Override
  public boolean handles(JSONValue o) {
    return o.isNumber() != null;
  }
}
