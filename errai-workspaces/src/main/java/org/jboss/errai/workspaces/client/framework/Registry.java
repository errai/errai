/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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
package org.jboss.errai.workspaces.client.framework;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for application wide services
 */
public class Registry
{
  private static Map<Class, Object> registry = new HashMap<Class,Object>();

   public static void set(Class key, Object obj)
   {
     registry.put(key, obj);
   }

   public static <T> T get(Class<T> key)
   {
     T t = (T) registry.get(key);
     if(null==t)
       throw new IllegalArgumentException(key + " not registered");
     return t;
   }

   public static boolean has(Class key)
   {
     return get(key)!=null;
   }
  
}
