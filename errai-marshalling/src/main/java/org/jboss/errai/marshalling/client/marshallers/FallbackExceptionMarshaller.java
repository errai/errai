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
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.util.MarshallUtil;

/**
 * this marshaller is intentionally not marked ServerMarshaller or ClientMarshaller
 * because it is used only to marshal exceptions not handled elsewhere
 */
public class FallbackExceptionMarshaller extends AbstractNullableMarshaller {

   public static final FallbackExceptionMarshaller INSTANCE = new FallbackExceptionMarshaller();

   @Override
   public Class getTypeHandled() {
      return Void.class;
   }

   @Override
   public Void[] getEmptyArray() {
      return new Void[0];
   }

   @Override
   public Object doNotNullDemarshall(final EJValue o, final MarshallingSession ctx) {
      return null;
   }

   @Override
   public String doNotNullMarshall(final Object o, final MarshallingSession ctx) {
      try {
         return "{\"" + SerializationParts.ENCODED_TYPE + "\":\"" + RuntimeException.class.getName() + "\","
                 + "\"" + SerializationParts.OBJECT_ID + "\":\"" + o.hashCode() + "\","
                 + "\"" + "message" + "\":\"" + MarshallUtil.jsonStringEscape(getCause(((Throwable) o)).getMessage()) + "\"}";
      } catch (Exception e) {
          throw new RuntimeException("no marshaller instance for: " + o.getClass());
      }
   }

   Throwable getCause(Throwable e) {
      if (e.getCause() != null) {
         return getCause(e.getCause());
      }
      return e;
   }
}
