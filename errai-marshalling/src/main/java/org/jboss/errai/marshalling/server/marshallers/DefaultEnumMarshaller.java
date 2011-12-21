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

package org.jboss.errai.marshalling.server.marshallers;

import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.json.EJObject;
import org.jboss.errai.marshalling.client.api.json.EJValue;

/**
 * @author Mike Brock
 */
public class DefaultEnumMarshaller implements Marshaller<Enum> {
  private Class enumType;

  public DefaultEnumMarshaller(Class enumType) {
    this.enumType = enumType;
  }

  @Override
  public Class<Enum> getTypeHandled() {
    return Enum.class;
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  public Enum demarshall(EJValue a0, MarshallingSession a1) {
       try {
         if (a0.isNull() != null) {
           return null;
         }
         EJObject obj = a0.isObject();
         Enum entity = Enum.valueOf(enumType, a0.isObject().get("EnumStringValue").isString().stringValue());
         return entity;
       } catch (Throwable t) {
         t.printStackTrace();
         throw new RuntimeException("error demarshalling entity: org.jboss.errai.bus.client.tests.support.TestEnumA", t);
       }
     }

     public String marshall(Enum a0, MarshallingSession a1) {
       if (a0 == null) {
         return "null";
       }
       return new StringBuilder().append("{\"__EncodedType\":\"org.jboss.errai.bus.client.tests.support.TestEnumA\",\"EnumStringValue\":\"").append(a0.name()).append("\"}").toString();
     }

  @Override
  public boolean handles(EJValue o) {
    return false;
  }
}
