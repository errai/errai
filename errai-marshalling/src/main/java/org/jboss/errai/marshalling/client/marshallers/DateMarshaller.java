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

import java.util.Date;

import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller;
import org.jboss.errai.marshalling.client.api.annotations.ServerMarshaller;
import org.jboss.errai.marshalling.client.api.json.EJValue;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ClientMarshaller @ServerMarshaller
public class DateMarshaller extends AbstractNullableMarshaller<Date> {
  
  private static final Date[] EMPTY_ARRAY = new Date[0];

  @Override
  public Class<Date> getTypeHandled() {
    return Date.class;
  }

  @Override
  public Date[] getEmptyArray() {
    return EMPTY_ARRAY;
  }
  
  @Override
  public Date doNotNullDemarshall(final EJValue o, final MarshallingSession ctx) {
    return new Date(Long.parseLong(o.isObject().get(SerializationParts.QUALIFIED_VALUE).isString().stringValue()));
  }

  @Override
  public String doNotNullMarshall(final Date o, final MarshallingSession ctx) {
    return "{\"" + SerializationParts.ENCODED_TYPE + "\":\"" + Date.class.getName() + "\"," +
            "\"" + SerializationParts.OBJECT_ID + "\":\"" + o.hashCode() + "\"," +
            "\"" + SerializationParts.QUALIFIED_VALUE + "\":\"" + o.getTime() + "\"}";
  }
}
