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

import static org.slf4j.LoggerFactory.getLogger;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.marshalling.client.MarshallingSessionProviderFactory;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallerFactory;
import org.jboss.errai.marshalling.client.api.MarshallingSession;
import org.jboss.errai.marshalling.client.api.Parser;
import org.jboss.errai.marshalling.client.api.ParserFactory;
import org.jboss.errai.marshalling.client.api.annotations.AlwaysQualify;
import org.jboss.errai.marshalling.client.api.exceptions.MarshallingException;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.client.marshallers.QualifyingMarshallerWrapper;
import org.jboss.errai.marshalling.client.protocols.MarshallingSessionProvider;
import org.jboss.errai.marshalling.client.util.EncDecUtil;
import org.jboss.errai.marshalling.rebind.DefinitionsFactory;
import org.jboss.errai.marshalling.rebind.DefinitionsFactorySingleton;
import org.jboss.errai.marshalling.rebind.api.model.MappingDefinition;
import org.jboss.errai.marshalling.rebind.api.model.MemberMapping;
import org.jboss.errai.marshalling.rebind.util.MarshallingGenUtil;
import org.jboss.errai.marshalling.server.marshallers.DefaultArrayMarshaller;
import org.jboss.errai.marshalling.server.util.ServerMarshallUtil;
import org.slf4j.Logger;

/**
 * @author Mike Brock
 */
public class MappingContextSingleton {
  private static final ServerMappingContext context;
  private static final Logger log = getLogger("ErraiMarshalling");

  static {
    ParserFactory.registerParser(
            new Parser() {
              @Override
              public EJValue parse(final String input) {
                return JSONDecoder.decode(input);
              }
            });

    ServerMappingContext sContext;

    try {
      if (!MarshallingGenUtil.isUseStaticMarshallers()) {
        sContext = loadDynamicMarshallers();
      }
      else {
        try {
          sContext = loadPrecompiledMarshallers();
        }
        catch (Throwable t) {
          log.debug("failed to load static marshallers", t);
          log.warn("static marshallers were not found.");

          sContext = loadDynamicMarshallers();
        }
      }
    }
    catch (Throwable t) {
      throw new RuntimeException("critical problem loading the marshallers", t);
    }

    context = sContext;
  }

  private static void dynamicMarshallingWarning() {
    log.warn("using dynamic marshallers. dynamic marshallers are designed" +
            " for development mode testing, and ideally should not be used in production. *");
  }

  public static ServerMappingContext loadPrecompiledMarshallers() throws Exception {

    final Class<? extends MarshallerFactory> cls
            = ServerMarshallUtil.getGeneratedMarshallerFactoryForServer();

    if (cls == null) {
      return loadDynamicMarshallers();
    }

    final Object o = cls.newInstance();
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
          public boolean hasMarshaller(final String fqcn) {
            return marshallerFactory.getMarshaller(null, fqcn) != null;
          }

          @Override
          public Marshaller getMarshaller(final String fqcn) {
            return marshallerFactory.getMarshaller(null, fqcn);
          }

        });
      }

      @Override
      public DefinitionsFactory getDefinitionsFactory() {
        return DefinitionsFactorySingleton.get();
      }

      @Override
      public Marshaller<Object> getMarshaller(final String clazz) {
        return marshallerFactory.getMarshaller(null, clazz);
      }

      @Override
      public boolean hasMarshaller(final String clazzName) {
        return marshallerFactory.getMarshaller(null, clazzName) != null;
      }

      @Override
      public boolean canMarshal(final String cls) {
        return hasMarshaller(cls);
      }
    };
  }

  public static ServerMappingContext loadDynamicMarshallers() {
    dynamicMarshallingWarning();

    return new ServerMappingContext() {
      private final DefinitionsFactory factory = DefinitionsFactorySingleton.newInstance();

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
          public boolean hasMarshaller(final String fqcn) {
            return factory.hasDefinition(fqcn);
          }

          @Override
          public Marshaller getMarshaller(final String fqcn) {
            return factory.getDefinition(fqcn).getMarshallerInstance();
          }
        });

        for (final MappingDefinition def : factory.getMappingDefinitions()) {
          if (def.getMarshallerInstance() != null) {
          }
          else if (def.getServerMarshallerClass() != null) {
            try {
              @SuppressWarnings("unchecked") final Marshaller<Object> marshallerInstance
                      = def.getServerMarshallerClass().asSubclass(Marshaller.class).newInstance();

              if (def.getServerMarshallerClass().isAnnotationPresent(AlwaysQualify.class)) {
                def.setMarshallerInstance(new QualifyingMarshallerWrapper<Object>(marshallerInstance));
              }
              else {
                def.setMarshallerInstance(marshallerInstance);
              }
            }
            catch (InstantiationException e) {
              e.printStackTrace();
            }
            catch (IllegalAccessException e) {
              e.printStackTrace();
            }
          }

          for (final MemberMapping mapping : def.getMemberMappings()) {
            if (mapping.getType().isArray()) {
              addArrayMarshaller(mapping.getType());
            }
          }
        }

        for (final MetaClass arrayType : MarshallingGenUtil.getDefaultArrayMarshallers()) {
          addArrayMarshaller(arrayType);
        }
      }

      private void addArrayMarshaller(final MetaClass type) {
        final MetaClass compType = type.getOuterComponentType();

        if (!factory.hasDefinition(type.getFullyQualifiedName())
                && !factory.hasDefinition(type.getInternalName())) {

          final MappingDefinition outerDef = factory.getDefinition(compType);
          final Marshaller<Object> marshaller = outerDef.getMarshallerInstance();

          if (marshaller == null) {
            System.out.println(outerDef.getMappingClass() + " has no registered marshaller; " +
            "marshCls=" + outerDef.getServerMarshallerClass());
          }

          final MappingDefinition newDef = new MappingDefinition(EncDecUtil.qualifyMarshaller(
                  new DefaultArrayMarshaller(type, marshaller)), true);

          newDef.setClientMarshallerClass(outerDef.getClientMarshallerClass());

          factory.addDefinition(newDef);
        }
      }


      @Override
      public DefinitionsFactory getDefinitionsFactory() {
        return factory;
      }

      @Override
      public Marshaller<Object> getMarshaller(final String clazz) {
        final MappingDefinition def = factory.getDefinition(clazz);

        if (def == null) {
          throw new MarshallingException("class is not available to the marshaller framework: " + clazz);
        }

        return def.getMarshallerInstance();
      }

      @Override
      public boolean hasMarshaller(final String clazzName) {
        return factory.hasDefinition(clazzName);
      }

      @Override
      public boolean canMarshal(final String cls) {
        return hasMarshaller(cls);
      }
    };
  }

  public static ServerMappingContext get() {
    return context;
  }
}
