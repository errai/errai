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

import com.google.gwt.json.client.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TypeDemarshallers {
  private static final Map<String, Demarshaller> classMap = new HashMap<String, Demarshaller>();
  private static final Map<Class, Demarshaller> demarshallers = new HashMap<Class, Demarshaller>();

  static {
//    addDemarshaller(java.util.Date.class, new Demarshaller() {
//      public Object demarshall(JSONObject o, DecodingContext decodingContext) {
//        String objId = o.get("__ObjectID").isString().stringValue();
//        if (decodingContext.hasObject(objId)) {
//          return decodingContext.getObject(objId);
//        }
//        else {
//          java.util.Date decDate = new java.util.Date((long) o.get("Value").isNumber().doubleValue());
//          decodingContext.putObject(objId, decDate);
//          return decDate;
//        }
//      }
//    });
//
//    addDemarshaller(java.sql.Date.class, new Demarshaller() {
//      public Object demarshall(JSONObject o, DecodingContext decodingContext) {
//        String objId = o.get("__ObjectID").isString().stringValue();
//        if (decodingContext.hasObject(objId)) {
//          return decodingContext.getObject(objId);
//        }
//        else {
//          java.sql.Date decDate = new java.sql.Date((long) o.get("Value").isNumber().doubleValue());
//          decodingContext.putObject(objId, decDate);
//          return decDate;
//        }
//      }
//    });


  }

//  public static void addDemarshaller(Class type, Demarshaller d) {
//    classMap.put(type.getName(), d);
//    demarshallers.put(type, d);
//  }
//
//  public static <T> Demarshaller<T> getDemarshaller(Class<? extends T> type) {
//    return demarshallers.get(type);
//  }
//
//  public static Demarshaller getDemarshaller(String type) {
//    return classMap.get(type);
//  }
//
//  public static boolean hasDemarshaller(Class type) {
//    return demarshallers.containsKey(type);
//  }
//
//  public static boolean hasDemarshaller(String type) {
//    return classMap.containsKey(type);
//  }
}
