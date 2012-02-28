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
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.annotations.ServerMarshaller;
import org.jboss.errai.marshalling.client.api.json.EJValue;

import java.math.BigInteger;

/**
 * @author Mike Brock
 */
@ClientMarshaller @ServerMarshaller
public class BigIntegerMarshaller extends AbstractJSONMarshaller<BigInteger> {
  @Override
  public BigInteger demarshall(EJValue o, MarshallingSession ctx) {
    if (o.isNull()) return null;

    return o.isObject() == null ? null :
            new BigInteger(o.isObject().get(SerializationParts.QUALIFIED_VALUE).isString().stringValue());
  }

  @Override
  public boolean handles(EJValue o) {
    return false;
  }

  @Override
  public Class<BigInteger> getTypeHandled() {
    return BigInteger.class;
  }

  @Override
  public String marshall(BigInteger o, MarshallingSession ctx) {
    if (o == null) { return "null"; }

    return "{\"" + SerializationParts.ENCODED_TYPE + "\":\"" + BigInteger.class.getName() + "\"," +
            "\"" + SerializationParts.OBJECT_ID + "\":\"" + o.hashCode() + "\"," +
            "\"" + SerializationParts.QUALIFIED_VALUE + "\":\"" + o.toString() + "\"}";
  }
}
