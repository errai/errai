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

import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.marshalling.client.api.MappingContext;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.annotations.ServerMarshaller;
import org.jboss.errai.marshalling.rebind.DefinitionsFactory;
import org.jboss.errai.marshalling.rebind.DefinitionsFactoryImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class MappingContextSingleton {
  private static final ServerMappingContext context;
  private static final Map<String, Class<?>> marshallerMap = new HashMap<String, Class<?>>();

  static {
    context = new ServerMappingContext() {
      private final DefinitionsFactory factory = new DefinitionsFactoryImpl();

      {
        loadMarshallers();
      }

      private void loadMarshallers() {
        Set<Class<?>> marshallers =
                ScannerSingleton.getOrCreateInstance().getTypesAnnotatedWith(ServerMarshaller.class);

        for (Class<?> m : marshallers) {
          try {
            Marshaller<Object, Object> marshaller = (Marshaller<Object, Object>) m.newInstance();

            registerMarshaller(marshaller.getTypeHandled().getName(), (Class<? extends Marshaller>) m);

          }
          catch (ClassCastException e) {
            throw new RuntimeException("@ServerMarshaller class "
                    + m.getName() + " is not an instance of " + Marshaller.class.getName());
          }
          catch (Throwable t) {
            throw new RuntimeException("Error instantiating " + m.getName(), t);
          }
        }
      }

      @Override
      public DefinitionsFactory getDefinitionsFactory() {
        return factory;
      }

      @Override
      public Class<? extends Marshaller> getMarshallerClass(String clazz) {
        return (Class<? extends Marshaller>) marshallerMap.get(clazz);
      }

      @Override
      public void registerMarshaller(String clazzName, Class<? extends Marshaller> clazz) {
        marshallerMap.put(clazzName, clazz);
      }

      @Override
      public boolean hasMarshaller(String clazzName) {
        return marshallerMap.containsKey(clazzName);
      }

      @Override
      public boolean canMarshal(String cls) {
        return hasMarshaller(cls);
      }
    };
  }

  public static ServerMappingContext get() {
    return context;
  }
}
