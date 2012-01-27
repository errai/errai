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
import org.jboss.errai.marshalling.client.MarshallingSessionProviderFactory;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallerFactory;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.Parser;
import org.jboss.errai.marshalling.client.api.ParserFactory;
import org.jboss.errai.marshalling.client.api.annotations.AlwaysQualify;
import org.jboss.errai.marshalling.client.api.annotations.ImplementationAliases;
import org.jboss.errai.marshalling.client.api.annotations.ServerMarshaller;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.marshallers.QualifyingMarshallerWrapper;
import org.jboss.errai.marshalling.client.protocols.MarshallingSessionProvider;
import org.jboss.errai.marshalling.client.util.EncDecUtil;
import org.jboss.errai.marshalling.client.util.MarshallUtil;
import org.jboss.errai.marshalling.rebind.DefinitionsFactory;
import org.jboss.errai.marshalling.rebind.DefinitionsFactoryImpl;
import org.jboss.errai.marshalling.rebind.api.model.MappingDefinition;
import org.jboss.errai.marshalling.rebind.api.model.MemberMapping;
import org.jboss.errai.marshalling.server.marshallers.DefaultArrayMarshaller;
import org.jboss.errai.marshalling.server.marshallers.DefaultDefinitionMarshaller;
import org.jboss.errai.marshalling.server.marshallers.DefaultEnumMarshaller;
import org.jboss.errai.marshalling.server.util.ServerMarshallUtil;

import java.util.Set;

/**
 * @author Mike Brock
 */
public class MappingContextSingleton {
  private static final ServerMappingContext context;
  // private static final MarshallerFactory marshallerFactory;

  static {
    ParserFactory.registerParser(
            new Parser() {
              @Override
              public EJValue parse(String input) {
                return JSONDecoder.decode(input);
              }
            });

    ServerMappingContext sContext;
    try {
      sContext = loadPrecompiledMarshallers();
    }
    catch (Throwable t) {
      sContext = loadDynamicMarshallers();
    }

//    sContext = loadDynamicMarshallers();
    context = sContext;
  }

  public static ServerMappingContext loadPrecompiledMarshallers() throws Exception {

    Object o = ServerMarshallUtil.getGeneratedMarshallerFactoryForServer().newInstance();
    final MarshallerFactory marshallerFactory = (MarshallerFactory) o;

    return new ServerMappingContext() {

      {
        MarshallingSessionProviderFactory.setMarshallingSessionProvider(new MarshallingSessionProvider() {
          @Override
          public MarshallingSession getEncoding() {
            return new EncodingSession(get());
          }

          @Override
          public MarshallingSession getDecoding() {
            return new DecodingSession(get());
          }

          @Override
          public boolean hasMarshaller(String fqcn) {
            return marshallerFactory.getMarshaller(null, fqcn) != null;
          }

          @Override
          public Marshaller getMarshaller(String fqcn) {
            return marshallerFactory.getMarshaller(null, fqcn);
          }
        });
      }


      @Override
      public DefinitionsFactory getDefinitionsFactory() {
        return null;
      }

      @Override
      public Marshaller<Object> getMarshaller(String clazz) {
        return marshallerFactory.getMarshaller(null, clazz);
      }

      @Override
      public boolean hasMarshaller(String clazzName) {
        return marshallerFactory.getMarshaller(null, clazzName) != null;
      }

      @Override
      public boolean canMarshal(String cls) {
        return hasMarshaller(cls);
      }
    };
  }

  public static ServerMappingContext loadDynamicMarshallers() {

    return new ServerMappingContext() {

      private final DefinitionsFactory factory = new DefinitionsFactoryImpl();

      {
        loadMarshallers();

        MarshallingSessionProviderFactory.setMarshallingSessionProvider(new MarshallingSessionProvider() {
          @Override
          public MarshallingSession getEncoding() {
            return new EncodingSession(get());
          }

          @Override
          public MarshallingSession getDecoding() {
            return new DecodingSession(get());
          }

          @Override
          public boolean hasMarshaller(String fqcn) {
            return factory.hasDefinition(fqcn);
          }

          @Override
          public Marshaller getMarshaller(String fqcn) {
            return factory.getDefinition(fqcn).getMarshallerInstance();
          }
        });
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
                      EncDecUtil.qualifyMarshaller(new DefaultArrayMarshaller(arrayType, marshaller))));

              /**
               * If this a pirmitive wrapper, create a special case for it using the same marshaller.
               */
              if (MarshallUtil.isPrimitiveWrapper(marshaller.getTypeHandled())) {
                factory.addDefinition(new MappingDefinition(
                        EncDecUtil.qualifyMarshaller(new DefaultArrayMarshaller(
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

            MappingDefinition def = factory.getDefinition(exposed);
            def.setMarshallerInstance(new DefaultDefinitionMarshaller(def));

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

            MappingDefinition definition = factory.getDefinition(exposed);

            for (MemberMapping mapping : definition.getMemberMappings()) {
              if (mapping.getType().isArray()) {
                MetaClass type = mapping.getType();
                MetaClass compType = type.getOuterComponentType();

                if (!factory.hasDefinition(type.getInternalName())) {
                  Marshaller<Object> marshaller = factory.getDefinition(compType).getMarshallerInstance();

                  def = new MappingDefinition(EncDecUtil.qualifyMarshaller(
                          new DefaultArrayMarshaller(type, marshaller)));

                  factory.addDefinition(def);
                }
              }
            }
          }
        }
      }

      @Override
      public DefinitionsFactory getDefinitionsFactory() {
        return factory;
      }

      @Override
      public Marshaller<Object> getMarshaller(String clazz) {
        return factory.getDefinition(clazz).getMarshallerInstance();
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
