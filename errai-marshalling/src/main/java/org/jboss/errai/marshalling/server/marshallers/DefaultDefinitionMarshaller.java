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

import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.util.NumbersUtils;
import org.jboss.errai.marshalling.rebind.api.model.*;
import org.jboss.errai.marshalling.server.EncodingSession;
import org.jboss.errai.marshalling.server.JSONStreamEncoder;
import org.jboss.errai.marshalling.server.TypeDemarshallHelper;
import org.jboss.errai.marshalling.server.api.ServerMarshaller;
import org.jboss.errai.marshalling.server.util.ServerEncodingUtil;
import org.mvel2.DataConversion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class DefaultDefinitionMarshaller implements ServerMarshaller<Object, Object> {
  private MappingDefinition definition;

  public DefaultDefinitionMarshaller(MappingDefinition definition) {
    this.definition = definition;
  }

  @Override
  public Class<Object> getTypeHandled() {
    return (Class<Object>) definition.getMappingClass().asClass();
  }

  @Override
  public String getEncodingType() {
    return "json";
  }

  @Override
  public Object demarshall(Object o, MarshallingSession ctx) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public String marshall(Object o, MarshallingSession ctx) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
    try {
      marshall(byteArrayOutputStream, o, ctx);
      return new String(byteArrayOutputStream.toByteArray());
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Object demarshallFromMap(Map<Object, Object> oMap, MarshallingSession ctx) throws Exception {
    Object newInstance;
    if (oMap.containsKey(SerializationParts.ENCODED_TYPE)) {
      if (oMap.containsKey(SerializationParts.NUMERIC_VALUE)) {
        return NumbersUtils.getNumber((String) oMap.get(SerializationParts.ENCODED_TYPE),
                oMap.get(SerializationParts.NUMERIC_VALUE));
      }

      String hash = (String) oMap.get(SerializationParts.OBJECT_ID);

      if (ctx.hasObjectHash(hash)) {
        newInstance = ctx.getObject(Object.class, hash);

        /**
         * If this only contains 2 fields, it is only a graph reference.
         */
        if (oMap.size() == 2) {
          return newInstance;
        }
      }
      else {
        if (oMap.containsKey(SerializationParts.INSTANTIATE_ONLY)) {
          return getTypeHandled().newInstance();
        }

        InstantiationMapping cMapping = definition.getInstantiationMapping();

        Object[] parms = new Object[cMapping.getMappings().length];
        Class[] targetTypes = cMapping.getSignature();

        int i = 0;
        for (Mapping mapping : cMapping.getMappings()) {
          parms[i] = DataConversion.convert(oMap.get(mapping.getKey()), targetTypes[i++]);
        }

        if (cMapping instanceof ConstructorMapping) {
          newInstance = ((ConstructorMapping) cMapping).getMember().asConstructor().newInstance(parms);
        }
        else {
          newInstance = ((FactoryMapping) cMapping).getMember().asMethod().invoke(null, parms);
        }
      }

      /**
       * In order toa accomedate the demarshaller's support for forward-references, detect the NO_AUTO_WIRE
       * hint and do not attempt to wire any mappings if its present.
       */
      if (!oMap.containsKey(TypeDemarshallHelper.NO_AUTO_WIRE)) {
        for (MemberMapping mapping : definition.getWritableMemberMappings()) {
          if (mapping.getBindingMember() instanceof MetaField) {
            MetaField f = (MetaField) mapping.getBindingMember();
            TypeDemarshallHelper.setProperty(newInstance, f.asField(), oMap.get(mapping.getKey()));
          }
          else {
            Method m = ((MetaMethod) mapping.getBindingMember()).asMethod();
            m.invoke(newInstance, DataConversion.convert(oMap.get(mapping.getKey()), m.getParameterTypes()[0]));
          }
        }
      }
    }
    else {
      throw new RuntimeException("unknown type to demarshall");
    }

    return newInstance;
  }

  @Override
  public void marshall(OutputStream outstream, Object o, MarshallingSession mSession) throws IOException {

    EncodingSession ctx = (EncodingSession) mSession;
    Class cls = o.getClass();
    boolean enc = ctx.isEncoded(o);
    String hash = ctx.getObjectHash(o);

    if (enc) {
      /**
       * If this object is referencing a duplicate object in the graph, we only provide an ID reference.
       */
      ServerEncodingUtil.write(outstream, ctx, "{\"" + SerializationParts.ENCODED_TYPE + "\":\"" + cls.getCanonicalName()
              + "\",\"" + SerializationParts.OBJECT_ID + "\":\"" + hash + "\"}");

      return;
    }

    int i = 0;
    boolean first = true;

    ServerEncodingUtil.write(outstream, ctx, "{\"" + SerializationParts.ENCODED_TYPE + "\":\"" + cls.getCanonicalName() + "\",\""
            + SerializationParts.OBJECT_ID + "\":\"" + hash + "\",");

    for (MemberMapping mapping : definition.getReadableMemberMappings()) {
      if (!first) {
        outstream.write(',');
      }

      i++;
      Object v;

      if (mapping.getReadingMember() instanceof MetaField) {
        Field field = ((MetaField) mapping.getReadingMember()).asField();

        try {
          v = field.get(o);
        }
        catch (Exception e) {
          throw new RuntimeException("error accessing field: " + field, e);
        }
      }
      else {
        Method method = ((MetaMethod) mapping.getReadingMember()).asMethod();

        try {
          v = method.invoke(o);
        }
        catch (Exception e) {
          throw new RuntimeException("error calling getter: " + method, e);
        }
      }

      ServerEncodingUtil.write(outstream, ctx, "\"" + mapping.getKey() + "\"");
      outstream.write(':');
      JSONStreamEncoder.encode(v, outstream, ctx);
      first = false;
    }

    if (i == 0) {
      ServerEncodingUtil.write(outstream, ctx, "\"" + SerializationParts.INSTANTIATE_ONLY + "\":true");
    }

    outstream.write('}');
  }

  @Override
  public boolean handles(Object o) {
    return false;
  }
}
