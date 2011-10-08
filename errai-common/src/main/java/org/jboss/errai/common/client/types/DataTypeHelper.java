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

package org.jboss.errai.common.client.types;

import com.google.gwt.json.client.*;
import org.jboss.errai.common.client.json.JSONDecoderCli;
import org.jboss.errai.common.client.protocols.SerializationParts;

import java.util.*;


public class DataTypeHelper {
  private static MarshallerProvider marshallerProvider;

  public static void setMarshallerProvider(MarshallerProvider provider) {
    marshallerProvider = provider;
  }

  public static MarshallerProvider getMarshallerProvider() {
    return marshallerProvider;
  }


  public static Object convert(JSONValue value, Class to, DecodingContext ctx) {
    JSONValue v;
    if ((v = value.isString()) != null) {
      return TypeHandlerFactory.convert(String.class, to, ((JSONString) v).stringValue(), ctx);
    }
    else if ((v = value.isNumber()) != null) {
      return TypeHandlerFactory.convert(Number.class, to, ((JSONNumber) v).doubleValue(), ctx);
    }
    else if ((v = value.isBoolean()) != null) {
      return TypeHandlerFactory.convert(Boolean.class, to, ((JSONBoolean) v).booleanValue(), ctx);
    }
    else if ((v = value.isArray()) != null) {
      List list = new ArrayList();
      JSONArray arr = (JSONArray) v;

      Class cType = to.getComponentType();

      while (cType != null && cType.getComponentType() != null)
        cType = cType.getComponentType();

      if (cType == null) cType = to;

      Object o;
      for (int i = 0; i < arr.size(); i++) {
        if ((o = convert(arr.get(i), cType, ctx)) instanceof UnsatisfiedForwardLookup) {
          ctx.addUnsatisfiedDependency(list, (UnsatisfiedForwardLookup) o);
        }
        else {
          list.add(convert(arr.get(i), cType, ctx));
        }
      }

      Object t = TypeHandlerFactory.convert(Collection.class, to, list, ctx);

      ctx.swapDepReference(list, t);

      return t;
    }
    else if ((v = value.isObject()) != null) {
      JSONObject eMap = (JSONObject) v;

      Map<Object, Object> m = new UHashMap<Object, Object>();
      Object o;
      Object val;

      for (String key : eMap.keySet()) {
        o = key;
        if (key.startsWith(SerializationParts.EMBEDDED_JSON)) {
          o = JSONDecoderCli.decode(key.substring(SerializationParts.EMBEDDED_JSON.length()), ctx);
        }
        else if (SerializationParts.ENCODED_TYPE.equals(key)) {
          String className = eMap.get(key).isString().stringValue();
          String objId = null;
          if ((v = eMap.get(SerializationParts.OBJECT_ID)) != null) {
            objId = v.isString().stringValue();
          }

          boolean ref = false;
          if (objId != null) {
            if (objId.charAt(0) == '$') {
              ref = true;
              objId = objId.substring(1);
            }

            if (ctx.hasObject(objId)) {
              return ctx.getObject(objId);
            }
            else if (ref) {
              return new UnsatisfiedForwardLookup(objId);
            }
          }

          if (marshallerProvider.hasMarshaller(className)) {
            return marshallerProvider.demarshall(className, eMap);
          }
//
//          if (TypeDemarshallers.hasDemarshaller(className)) {
//            o = TypeDemarshallers.getDemarshaller(className).demarshall(eMap, ctx);
//            if (objId != null) ctx.putObject(objId, o);
//            return o;
//          }
          else {
            throw new RuntimeException("no available demarshaller: " + className);
          }
        }

        val = JSONDecoderCli.decode(eMap.get(key), ctx);
        boolean commit = true;

        if (o instanceof UnsatisfiedForwardLookup) {
          ctx.addUnsatisfiedDependency(m, (UnsatisfiedForwardLookup) o);
          if (!(val instanceof UnsatisfiedForwardLookup)) {
            ((UnsatisfiedForwardLookup) o).setVal(val);
          }
          commit = false;
        }
        if (val instanceof UnsatisfiedForwardLookup) {
          ((UnsatisfiedForwardLookup) val).setKey(o);
          ctx.addUnsatisfiedDependency(m, (UnsatisfiedForwardLookup) val);
          commit = false;
        }

        if (commit) {
          m.put(o, JSONDecoderCli.decode(eMap.get(key), ctx));
        }

      }

      return TypeHandlerFactory.convert(Map.class, to, m, ctx);
    }

    return null;
  }

  public static void resolveDependencies(DecodingContext ctx) {
    try {
      for (Map.Entry<Object, List<UnsatisfiedForwardLookup>> entry : ctx.getUnsatisfiedDependencies().entrySet()) {
        if (entry.getValue() == null) continue;

        Iterator<UnsatisfiedForwardLookup> iter = entry.getValue().iterator();
        if (entry.getKey() instanceof Collection) {
          while (iter.hasNext()) {
            ((Collection<Object>) entry.getKey()).add(ctx.getObject(iter.next().getId()));
          }
        }
        else if (entry.getKey() instanceof Map && !((Map) entry.getKey()).containsKey(SerializationParts.ENCODED_TYPE)) {
          UnsatisfiedForwardLookup u1 = iter.next();
          if (!iter.hasNext()) {
            if (u1.getKey() != null) {
              if (u1.getKey() instanceof UnsatisfiedForwardLookup) {
                ((Map<Object, Object>) entry.getKey()).put(ctx.getObject(((UnsatisfiedForwardLookup) u1.getKey()).getId()), ctx.getObject(u1.getId()));
              }
              else {
                ((Map<Object, Object>) entry.getKey()).put(u1.getKey(), ctx.getObject(u1.getId()));
              }
            }
            else if (u1.getVal() != null) {
              ((Map<Object, Object>) entry.getKey()).put(ctx.getObject(u1.getId()), u1.getVal());
            }
            else {
              throw new RuntimeException("error resolving dependencies in payload (Map Element): " + u1.getId());
            }
          }
          else {
            UnsatisfiedForwardLookup u2 = iter.next();
            ((Map<Object, Object>) entry.getKey()).put(ctx.getObject(u1.getId()), ctx.getObject(u2.getId()));
          }
        }
        else {
          UnsatisfiedForwardLookup ufl;
          while (iter.hasNext()) {
            if ((ufl = iter.next()).getBinder() == null) {
              throw new RuntimeException("cannot satisfy dependency in object graph (path unresolvable):" + ufl.getId());
            }
            else {
              ufl.getBinder().bind(ctx.getObject(ufl.getId()));
            }
          }
        }

        if (entry.getKey() instanceof UHashMap)
          ((UHashMap) entry.getKey()).normalHashMode();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("error resolving dependenceis", e);
    }
  }

  public static String encodeHelper(Object v) {
    if (v instanceof String) {
      return "\\\"" + v + "\\\"";
    }
    else if (v instanceof Character) {
      return "'" + v + "'";
    }
    else {
      return String.valueOf(v);
    }
  }
}
