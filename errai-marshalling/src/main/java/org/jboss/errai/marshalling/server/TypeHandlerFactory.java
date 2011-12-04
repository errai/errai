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

package org.jboss.errai.marshalling.server;

import org.jboss.errai.common.client.types.TypeHandler;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class TypeHandlerFactory {
  private static final Map<Class, TypeHandler> TYPE_HANDLERS = new HashMap<Class, TypeHandler>();
  
  static {
    registerHandler(Timestamp.class, new TypeHandler<Timestamp, Long>() {
      public Long getConverted(Timestamp in) {
        return in.getTime();
      }
    });

    registerHandler(Character.class, new TypeHandler<Character, String>() {
      public String getConverted(Character in) {
        return String.valueOf(in.charValue());
      }
    });
    
    registerHandler(java.sql.Date.class, new TypeHandler<java.sql.Date, Long>() {
      public Long getConverted(java.sql.Date in) {
        return in.getTime();
      }
    });
    
    registerHandler(java.util.Date.class, new TypeHandler<java.util.Date, Long>() {
      public Long getConverted(java.util.Date in) {
        return in.getTime();
      }
    });

    registerHandler(StringBuilder.class, new TypeHandler<StringBuffer, String>() {
      @Override
      public String getConverted(StringBuffer in) {
        return in.toString();
      }
    });
    
    registerHandler(StringBuilder.class, new TypeHandler<StringBuilder, String>() {
      @Override
      public String getConverted(StringBuilder in) {
        return in.toString();
      }
    });
  }
  
//  public static boolean hasHandler(Class cls) {
//    return TYPE_HANDLERS.containsKey(cls);
//  }

  public static void registerHandler(Class cls, TypeHandler handler) {
    TYPE_HANDLERS.put(cls, handler);
  }

  public static <T> TypeHandler<T, Object> getHandler(Class<T> handlerType) {
    return TYPE_HANDLERS.get(handlerType);
  }
}
