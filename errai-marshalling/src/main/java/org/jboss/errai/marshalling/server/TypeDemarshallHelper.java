/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.marshalling.server;

import com.google.gwt.dev.shell.JsValue;
import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.json.EJObject;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.rebind.DefinitionsFactory;
import org.jboss.errai.marshalling.rebind.api.model.*;
import org.jboss.errai.marshalling.server.api.ServerMarshaller;
import org.jboss.errai.marshalling.server.json.impl.ErraiJSONValue;
import org.mvel2.ConversionHandler;
import org.mvel2.DataConversion;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static org.mvel2.DataConversion.addConversionHandler;

public class TypeDemarshallHelper {

  /**
   * This is a hint for marshallers to detect that they should instantiate only and not wire any further mappings.
   */
  public static final String NO_AUTO_WIRE = "__NoAutoWire";

  static {
    addConversionHandler(java.sql.Date.class, new ConversionHandler() {
      public Object convertFrom(Object o) {
        if (o instanceof String) o = Long.parseLong((String) o);

        return new java.sql.Date(((Number) o).longValue());
      }

      public boolean canConvertFrom(Class aClass) {
        return Number.class.isAssignableFrom(aClass);
      }
    });

    addConversionHandler(java.util.Date.class, new ConversionHandler() {
      public Object convertFrom(Object o) {
        if (o instanceof String) o = Long.parseLong((String) o);
        return new java.util.Date(((Number) o).longValue());
      }

      public boolean canConvertFrom(Class aClass) {
        return Number.class.isAssignableFrom(aClass);
      }
    });

    addConversionHandler(StringBuilder.class, new ConversionHandler() {
      public Object convertFrom(Object o) {
        //  if (o instanceof String) o = Long.parseLong((String) o);
        return new StringBuilder((String) o);
      }

      public boolean canConvertFrom(Class aClass) {
        return CharSequence.class.isAssignableFrom(aClass);
      }
    });

    addConversionHandler(StringBuffer.class, new ConversionHandler() {
      public Object convertFrom(Object o) {
        //  if (o instanceof String) o = Long.parseLong((String) o);
        return new StringBuffer((String) o);
      }

      public boolean canConvertFrom(Class aClass) {
        return CharSequence.class.isAssignableFrom(aClass);
      }
    });
  }

  public static Class getClassReference(EJObject oMap) {
    try {
      return Thread.currentThread().getContextClassLoader().loadClass(oMap.get(SerializationParts.ENCODED_TYPE).isString().stringValue());
    }
    catch (ClassNotFoundException e) {
      throw new RuntimeException("could not instantiate class", e);
    }
  }

  public static Object instantiate(EJObject oMap, DecodingSession ctx) {
    return instantiate(getClassReference(oMap), oMap, ctx);
  }

  public static Object instantiate(Class clazz, EJObject oMap, DecodingSession ctx) {
    try {
      String hash = oMap.get(SerializationParts.OBJECT_ID).isString().stringValue();

      if (ctx.hasObjectHash(hash)) {
        return ctx.getObject(Object.class, hash);
      }

      if (clazz.isEnum()) {
        return Enum.valueOf(clazz, oMap.get(SerializationParts.ENUM_STRING_VALUE).isString().stringValue());
      }

      DefinitionsFactory defs = ctx.getMappingContext().getDefinitionsFactory();

      Object o;
      if (defs.hasDefinition(clazz)) {
        MappingDefinition def = defs.getDefinition(clazz);
        if (def.isCachedMarshaller()) {

          return callMarshaller(def.getMarshallerInstance(), new ErraiJSONValue(oMap), ctx);
        }

        InstantiationMapping cns = def.getInstantiationMapping();
        Mapping[] mappings = cns.getMappings();
        Object[] parms = new Object[mappings.length];
        Class[] elTypes = cns.getSignature();

        for (int i = 0; i < mappings.length; i++) {
          parms[i] = DataConversion.convert(oMap.get(mappings[i].getKey()), elTypes[i]);
        }

        if (cns instanceof ConstructorMapping) {
          o = ((ConstructorMapping) cns).getMember().asConstructor().newInstance(parms);
        }
        else {
          o = ((FactoryMapping) cns).getMember().asMethod().invoke(null, parms);
        }
      }
      else {
        o = clazz.newInstance();
      }
      return o;
    }
    catch (Throwable e) {
      throw new RuntimeException("error demarshalling", e);
    }
  }


  private static Object callMarshaller(Marshaller m, EJValue oMap, DecodingSession ctx) throws Exception {
    return m.demarshall(oMap, ctx);
  }

  public static Object demarshallAll(EJValue o, DecodingSession ctx) throws Exception {
    try {
      if (o.isString() != null) {
        return o.isString().stringValue();
      }
      else if (o.isObject() != null) {
        EJObject obj = o.isObject();

        Class cls = getClassReference(obj);

        if (cls.isEnum()) {
          return Enum.valueOf(cls, obj.get(SerializationParts.ENUM_STRING_VALUE).isString().stringValue());
        }

        if (ctx.getMappingContext().getDefinitionsFactory().hasDefinition(cls)) {
          Marshaller<Object> marshaller = ctx.getMappingContext().getDefinitionsFactory().getDefinition(cls)
                  .getMarshallerInstance();

          return callMarshaller(marshaller, o, ctx);
        }
        else {
          throw new RuntimeException("unknown class to demarshall: " + cls.getName());
        }
      }

      return o;
    }
    catch (Exception e) {
      throw new RuntimeException("error demarshalling encoded object:\n" + o, e);
    }
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

  public static String ensureSafe(String s) {
    for (int i = 0; i < s.length(); i++) {
      if (!Character.isJavaIdentifierPart(s.charAt(i))) {
        throw new RuntimeException("illegal character in property");
      }
    }
    return s;
  }

  public static Long getNumeric(Object val) {
    Long longVal;
    if (val instanceof String) {
      longVal = Long.parseLong(String.valueOf(val));
    }
    else if (val instanceof Long) {
      longVal = (Long) val;
    }
    else {
      throw new RuntimeException("expected number: " + val);
    }

    return longVal;
  }
}
