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
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.exceptions.MarshallingException;
import org.jboss.errai.marshalling.client.api.json.EJObject;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.util.NumbersUtils;
import org.jboss.errai.marshalling.rebind.api.model.ConstructorMapping;
import org.jboss.errai.marshalling.rebind.api.model.FactoryMapping;
import org.jboss.errai.marshalling.rebind.api.model.InstantiationMapping;
import org.jboss.errai.marshalling.rebind.api.model.Mapping;
import org.jboss.errai.marshalling.rebind.api.model.MappingDefinition;
import org.jboss.errai.marshalling.rebind.api.model.MemberMapping;
import org.jboss.errai.marshalling.server.EncodingSession;
import org.jboss.errai.marshalling.server.MappingContextSingleton;
import org.jboss.errai.marshalling.server.api.ServerMarshaller;
import org.mvel2.DataConversion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Mike Brock
 */
public class DefaultDefinitionMarshaller implements ServerMarshaller<Object> {

  private MappingDefinition definition;

  public DefaultDefinitionMarshaller(MappingDefinition definition) {
    this.definition = definition;
  }

  public static void setProperty(Object i, Field f, Object v) {
    try {
      f.setAccessible(true);
      f.set(i, DataConversion.convert(v, f.getType()));
    }
    catch (Exception e) {
      throw new RuntimeException("could not set field (inst=" + i + "; field=" + f + "; val=" + v + ")", e);
    }
  }

  @Override
  public Class<Object> getTypeHandled() {
    return (Class<Object>) definition.getMappingClass().asClass();
  }

  @Override
  public Object demarshall(EJValue o, MarshallingSession ctx) {
    try {
      if (o.isObject() != null) {
        EJObject oMap = o.isObject();

        Object newInstance;
        if (oMap.containsKey(SerializationParts.OBJECT_ID)) {
          if (oMap.containsKey(SerializationParts.NUMERIC_VALUE)) {
            return NumbersUtils.getNumber(oMap.get(SerializationParts.ENCODED_TYPE).isString().stringValue(),
                    oMap.get(SerializationParts.NUMERIC_VALUE));
          }

          if (oMap.containsKey(SerializationParts.OBJECT_ID)) {
            String hash = oMap.get(SerializationParts.OBJECT_ID).isString().stringValue();

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
                newInstance = getTypeHandled().newInstance();
                ctx.recordObjectHash(hash, newInstance);
                return newInstance;
              }

              InstantiationMapping cMapping = definition.getInstantiationMapping();

              Object[] parms = new Object[cMapping.getMappings().length];
              Class[] targetTypes = cMapping.getSignature();

              int i = 0;
              for (Mapping mapping : cMapping.getMappings()) {
                Marshaller<Object> marshaller = ctx.getMarshallerInstance(mapping.getType().getFullyQualifiedName());
                parms[i] = DataConversion.convert(
                        marshaller.demarshall(oMap.get(mapping.getKey()), ctx), targetTypes[i++]);
              }

              if (cMapping instanceof ConstructorMapping) {
                newInstance = ((ConstructorMapping) cMapping).getMember().asConstructor().newInstance(parms);
              }
              else {
                newInstance = ((FactoryMapping) cMapping).getMember().asMethod().invoke(null, parms);
              }

              ctx.recordObjectHash(hash, newInstance);
            }

            for (MemberMapping mapping : definition.getWritableMemberMappings()) {
              Marshaller<Object> marshaller = ctx.getMarshallerInstance(mapping.getType().getFullyQualifiedName());

              if (mapping.getBindingMember() instanceof MetaField) {
                MetaField f = (MetaField) mapping.getBindingMember();

                setProperty(newInstance, f.asField(),
                        marshaller.demarshall(oMap.get(mapping.getKey()), ctx));
              }
              else {
                Method m = ((MetaMethod) mapping.getBindingMember()).asMethod();
                m.invoke(newInstance, DataConversion.convert(
                        marshaller.demarshall(oMap.get(mapping.getKey()), ctx),
                        m.getParameterTypes()[0]));
              }
            }
          }
          else {
            throw new RuntimeException("unknown type to demarshall");
          }

          return newInstance;
        }
        else if (oMap.containsKey(SerializationParts.ENUM_STRING_VALUE)) {
          return Enum.valueOf(getClassReference(oMap), oMap.get(SerializationParts.ENUM_STRING_VALUE).isString().stringValue());
        }
        else {
          throw new RuntimeException("bad payload");
        }
      }

      else {
        return o.getRawValue();
      }
    }
    catch (Exception e) {
      throw new MarshallingException("Failed to demarshall an instance of " + definition.getMappingClass(), e);
    }
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
  public void marshall(final OutputStream outstream, Object o, MarshallingSession mSession) throws IOException {

    if (o == null) {
      outstream.write("null".getBytes());
      return;
    }

    EncodingSession ctx = (EncodingSession) mSession;
    Class cls = o.getClass();

    if (o instanceof Enum) {
      Enum enumer = (Enum) o;

      outstream.write(("{\"" + SerializationParts.ENCODED_TYPE + "\":\""
              + enumer.getDeclaringClass().getName() + "\""
              + ",\"" + SerializationParts.ENUM_STRING_VALUE + "\":\"" + enumer.name() + "\"}")
              .getBytes());

      return;
    }


    boolean enc = ctx.isEncoded(o);
    String hash = ctx.getObjectHash(o);

    if (enc) {
      /**
       * If this object is referencing a duplicate object in the graph, we only provide an ID reference.
       */

      outstream.write(("{\"" + SerializationParts.ENCODED_TYPE + "\":\"" + cls.getName()
              + "\",\"" + SerializationParts.OBJECT_ID + "\":\"" + hash + "\"}").getBytes());

      return;
    }

    int i = 0;
    boolean first = true;

    outstream.write(("{\"" + SerializationParts.ENCODED_TYPE + "\":\"" + cls.getName() + "\",\""
            + SerializationParts.OBJECT_ID + "\":\"" + hash + "\",").getBytes());


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

      outstream.write(("\"" + mapping.getKey() + "\"").getBytes());

      outstream.write(':');
      outstream.write(
              MappingContextSingleton.get().getDefinitionsFactory()
                      .getDefinition(mapping.getType()).getMarshallerInstance().marshall(v, ctx)
                      .getBytes()
      );

      first = false;
    }

    if (i == 0) {
      outstream.write(("\"" + SerializationParts.INSTANTIATE_ONLY + "\":true").getBytes());
    }

    outstream.write('}');
  }

  public static Class getClassReference(EJObject oMap) {
    try {
      return Thread.currentThread().getContextClassLoader().loadClass(oMap.get(SerializationParts.ENCODED_TYPE).isString().stringValue());
    }
    catch (ClassNotFoundException e) {
      throw new RuntimeException("could not instantiate class", e);
    }
  }

}
