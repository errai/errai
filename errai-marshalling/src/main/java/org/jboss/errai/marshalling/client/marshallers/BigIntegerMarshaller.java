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

import java.math.BigInteger;

import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.annotations.ServerMarshaller;
import org.jboss.errai.marshalling.client.api.json.EJValue;

/**
 * @author Mike Brock
 */
@ClientMarshaller @ServerMarshaller
public class BigIntegerMarshaller extends AbstractNullableMarshaller<BigInteger> {

  private static final BigInteger[] EMPTY_ARRAY = new BigInteger[0];

  @Override
  public Class<BigInteger> getTypeHandled() {
    return BigInteger.class;
  }
  
  @Override
  public BigInteger[] getEmptyArray() {
    return EMPTY_ARRAY;
  }
  
  @Override
  public BigInteger doNotNullDemarshall(EJValue o, MarshallingSession ctx) {
    return o.isObject() == null ? null :
            new BigInteger(o.isObject().get(SerializationParts.QUALIFIED_VALUE).isString().stringValue());
  }

  @Override
  public String doNotNullMarshall(BigInteger o, MarshallingSession ctx) {
    return "{\"" + SerializationParts.ENCODED_TYPE + "\":\"" + BigInteger.class.getName() + "\"," +
            "\"" + SerializationParts.OBJECT_ID + "\":\"" + o.hashCode() + "\"," +
            "\"" + SerializationParts.QUALIFIED_VALUE + "\":\"" + o.toString() + "\"}";
  }
}
