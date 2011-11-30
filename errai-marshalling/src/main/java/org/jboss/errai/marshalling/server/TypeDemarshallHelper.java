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

import org.jboss.errai.common.client.protocols.SerializationParts;
import org.jboss.errai.common.client.types.DecodingContext;
import org.jboss.errai.common.client.types.UnsatisfiedForwardLookup;
import org.jboss.errai.marshalling.client.util.NumbersUtils;
import org.mvel2.ConversionHandler;
import org.mvel2.DataConversion;
import org.mvel2.MVEL;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.mvel2.DataConversion.addConversionHandler;

public class TypeDemarshallHelper {
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
  }

  private static final Map<Class, Map<String, Serializable>> MVELDencodingCache = new ConcurrentHashMap<Class, Map<String, Serializable>>();

  public static Object instantiate(Map oMap, DecodingContext ctx) {
    try {
            
      Class clazz = Thread.currentThread().getContextClassLoader().loadClass((String) oMap.get(SerializationParts.ENCODED_TYPE));
      String objId = (String) oMap.get(SerializationParts.OBJECT_ID);

      if (ctx.hasObject(objId)) {
        return ctx.getObject(objId);
      }

      if (clazz.isEnum()) {
        return Enum.valueOf(clazz, (String) oMap.get(SerializationParts.ENUM_STRING_VALUE));
      }
      else if (java.util.Date.class.isAssignableFrom(clazz)) {
        return new java.util.Date(getNumeric(oMap.get("Value")));
      }
      else if (java.sql.Date.class.isAssignableFrom(clazz)) {
        return new java.sql.Date(getNumeric(oMap.get("Value")));
      }
      
      Object newInstance = clazz.newInstance();
      if (objId != null) ctx.putObject(objId, newInstance);
      
      return newInstance;

    }
    catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    catch (InstantiationException e) {
      e.printStackTrace();
    }
    catch (IllegalAccessException e) {
      e.printStackTrace();
    }

    return null;
  }


  public static Object demarshallAll(Object o, DecodingContext ctx) throws Exception {
    try {
      if (o instanceof String) {
        return o;

      }
      else if (o instanceof Collection) {
        ArrayList newList = new ArrayList(((Collection) o).size());
        Object dep;
        for (Object o2 : ((Collection) o)) {
          if ((dep = demarshallAll(o2, ctx)) instanceof UnsatisfiedForwardLookup) {
            ctx.addUnsatisfiedDependency(o, (UnsatisfiedForwardLookup) dep);
          }
          else {
            newList.add(dep);
          }
        }

        if (ctx.hasUnsatisfiedDependency(o)) {
          ctx.swapDepReference(o, newList);
        }

        return newList;
      }
      else if (o instanceof Map) {
        Map<?, ?> oMap = (Map) o;
        if (oMap.containsKey(SerializationParts.ENCODED_TYPE)) {
          if (oMap.containsKey(SerializationParts.NUMERIC_VALUE)) {
            return NumbersUtils.getNumber((String) oMap.get(SerializationParts.ENCODED_TYPE),
                    oMap.get(SerializationParts.NUMERIC_VALUE));
          }

          Object newInstance = instantiate(oMap, ctx);
          
          if (ctx.hasUnsatisfiedDependency(o)) {
            ctx.swapDepReference(o, newInstance);
          }

          if (oMap.size() == 2 && !oMap.containsKey(SerializationParts.INSTANTIATE_ONLY)) {
            return newInstance;
          }

          for (Field f : EncodingUtil.getAllEncodingFields(newInstance.getClass())) {
            setProperty(newInstance, f, oMap.get(f.getName()));
          }

          return newInstance;
        }
      }
      return o;
    }
    catch (Exception e) {
      throw new RuntimeException("error demarshalling encoded object:\n" + o, e);
    }
  }

  @SuppressWarnings({"unchecked"})
  public static void resolveDependencies(DecodingContext ctx) {

  }

  public static Serializable compileSetExpression(String s) {
    return MVEL.compileSetExpression(ensureSafe(s));
  }

  public static void setProperty(Object i, Field f, Object v) {

    try {
      f.setAccessible(true);
      f.set(i, DataConversion.convert(v, f.getType()));
    }
    catch (Exception e) {
      throw new RuntimeException("could not set field", e);
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
