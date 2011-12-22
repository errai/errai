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
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.Parser;
import org.jboss.errai.marshalling.client.api.ParserFactory;
import org.jboss.errai.marshalling.client.api.annotations.AlwaysQualify;
import org.jboss.errai.marshalling.client.api.annotations.ImplementationAliases;
import org.jboss.errai.marshalling.client.api.annotations.ServerMarshaller;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.marshallers.QualifyingMarshallerWrapper;
import org.jboss.errai.marshalling.client.protocols.ErraiProtocol;
import org.jboss.errai.marshalling.client.protocols.MarshallingSessionProvider;
import org.jboss.errai.marshalling.client.util.MarshallUtil;
import org.jboss.errai.marshalling.rebind.DefinitionsFactory;
import org.jboss.errai.marshalling.rebind.DefinitionsFactoryImpl;
import org.jboss.errai.marshalling.rebind.api.model.Mapping;
import org.jboss.errai.marshalling.rebind.api.model.MappingDefinition;
import org.jboss.errai.marshalling.server.marshallers.DefaultArrayMarshaller;
import org.jboss.errai.marshalling.server.marshallers.DefaultEnumMarshaller;

import java.util.Set;

import static org.jboss.errai.marshalling.client.util.EncDecUtil.qualifyMarshaller;

/**
 * @author Mike Brock
 */
public class MappingContextSingleton {
  private static final ServerMappingContext context;

  static {
    ParserFactory.registerParser(
            new Parser() {
              @Override
              public EJValue parse(String input) {
                return JSONDecoder.decode(input);
              }
            }
    );
    
    ErraiProtocol.setMarshallingSessionProvider(new MarshallingSessionProvider() {
      @Override
      public MarshallingSession getEncoding() {
        return new EncodingSession(get());
      }

      @Override
      public MarshallingSession getDecoding() {
        return new DecodingSession(get());
      }
    });

    context = new ServerMappingContext() {
      private final DefinitionsFactory factory = new DefinitionsFactoryImpl();

      {
        loadMarshallers();

        for (Class<?> cls : factory.getExposedClasses()) {
          MappingDefinition def = factory.getDefinition(cls);

          for (Mapping m : def.getInstantiationMapping().getMappings()) {
            if (m.getTargetType().isArray() && !factory.hasDefinition(m.getTargetType())) {
              MappingDefinition arrayMappingDefinition = new MappingDefinition(m.getTargetType());
              arrayMappingDefinition.setMarshallerInstance(
                      qualifyMarshaller(new DefaultArrayMarshaller(m.getTargetType(),
                              factory.getDefinition(m.getTargetType().getOuterComponentType()).getMarshallerInstance()))
              );

              factory.addDefinition(arrayMappingDefinition);
            }
          }

          for (Mapping m : def.getMemberMappings()) {
            if (m.getTargetType().isArray() && !factory.hasDefinition(m.getTargetType())) {
              MappingDefinition arrayMappingDefinition = new MappingDefinition(m.getTargetType());
              arrayMappingDefinition.setMarshallerInstance(
                      qualifyMarshaller(new DefaultArrayMarshaller(m.getTargetType(),
                              factory.getDefinition(m.getTargetType().getOuterComponentType().asBoxed())
                                      .getMarshallerInstance()))
              );

              factory.addDefinition(arrayMappingDefinition);
            }
          }
        }

      }

      private void loadMarshallers() {
        Set<Class<?>> marshallers =
                ScannerSingleton.getOrCreateInstance().getTypesAnnotatedWith(ServerMarshaller.class);

        for (Class<?> m : marshallers) {
          try {
            Marshaller<Object> marshaller = (Marshaller<Object>) m.newInstance();

            if (m.isAnnotationPresent(AlwaysQualify.class)) {
              marshaller = new QualifyingMarshallerWrapper(marshaller);
            }

            factory.addDefinition(new MappingDefinition(marshaller));

            if (m.isAnnotationPresent(ImplementationAliases.class)) {
              for (Class<?> inherits : m.getAnnotation(ImplementationAliases.class).value()) {
                factory.addDefinition(new MappingDefinition(marshaller, inherits));
              }
            }

            /**
             * Load the default array marshaller.
             */
            MetaClass arrayType = MetaClassFactory.get(marshaller.getTypeHandled()).asArrayOf(1);
            if (!factory.hasDefinition(arrayType)) {
              factory.addDefinition(new MappingDefinition(
                      qualifyMarshaller(new DefaultArrayMarshaller(arrayType, marshaller))));

              /**
               * If this a pirmitive wrapper, create a special case for it using the same marshaller.
               */
              if (MarshallUtil.isPrimitiveWrapper(marshaller.getTypeHandled())) {
                factory.addDefinition(new MappingDefinition(
                        qualifyMarshaller(new DefaultArrayMarshaller(
                                arrayType.getOuterComponentType().asUnboxed().asArrayOf(1), marshaller))));
              }
            }

          }
          catch (ClassCastException e) {
            throw new RuntimeException("@ServerMarshaller class "
                    + m.getName() + " is not an instance of " + Marshaller.class.getName());
          }
          catch (Throwable t) {
            throw new RuntimeException("Error instantiating " + m.getName(), t);
          }
        }

        for (Class<?> exposed : factory.getExposedClasses()) {
          if (exposed.isAnnotationPresent(Portable.class)) {
            Portable p = exposed.getAnnotation(Portable.class);

            if (!p.aliasOf().equals(Object.class)) {
              if (!factory.hasDefinition(p.aliasOf())) {
                throw new RuntimeException("cannot alias " + exposed.getName() + " to unmapped type: "
                        + p.aliasOf().getName());
              }

              factory.getDefinition(exposed)
                      .setMarshallerInstance(factory.getDefinition(p.aliasOf()).getMarshallerInstance());
            }

            if (exposed.isEnum()) {
              factory.getDefinition(exposed)
                      .setMarshallerInstance(new DefaultEnumMarshaller(exposed));
            }
          }
        }
      }

      @Override
      public DefinitionsFactory getDefinitionsFactory() {
        return factory;
      }

      @Override
      public Class<? extends Marshaller> getMarshallerClass(String clazz) {
        return factory.getDefinition(clazz).getServerMarshallerClass();
      }

      @Override
      public boolean hasMarshaller(String clazzName) {
        return factory.hasDefinition(clazzName);
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
