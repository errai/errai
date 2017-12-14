/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.marshalling.rebind;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.IncrementalGenerator;
import com.google.gwt.core.ext.RebindMode;
import com.google.gwt.core.ext.RebindResult;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import org.apache.commons.lang3.StringUtils;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.impl.AbstractMetaClass;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.config.ErraiConfiguration;
import org.jboss.errai.config.MetaClassFinder;
import org.jboss.errai.config.propertiesfile.ErraiAppPropertiesConfiguration;
import org.jboss.errai.marshalling.client.api.MarshallerFramework;
import org.jboss.errai.marshalling.rebind.api.GeneratorMappingContext;
import org.jboss.errai.marshalling.rebind.api.GeneratorMappingContextFactory;
import org.jboss.errai.marshalling.rebind.api.MappingStrategy;
import org.jboss.errai.marshalling.rebind.util.MarshallingGenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generator used to generate marshallers for custom portable types
 * independently. In DevMode, generation is deferred until the marshaller is
 * actually needed. This is also an incremental generator. It will only generate
 * code when a portable type has changed or a new one has been introduced.
 * Otherwise, it will use a cached version of the generated marshaller code.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class MarshallerGenerator extends IncrementalGenerator {
  private static final Logger log = LoggerFactory.getLogger(MarshallerGenerator.class);
  public static final String PACKAGE_NAME = MarshallerFramework.class.getPackage().getName();

  // We're keeping this cache of portable types to compare their contents and
  // find out if they have changed since the last refresh.
  private static Map<String, MetaClass> cachedPortableTypes = new ConcurrentHashMap<>();
  private static Map<String, String> cachedSourceByTypeName = new ConcurrentHashMap<>();

  /*
   * A version id. Increment this as needed, when structural changes are made to
   * the generated output, specifically with respect to it's effect on the
   * caching and reuse of previous generator results. Previously cached
   * generator results will be invalidated automatically if they were generated
   * by a version of this generator with a different version id.
   */
  private static final long GENERATOR_VERSION_ID = 1L;

  @Override
  public RebindResult generateIncrementally(final TreeLogger logger,
          final GeneratorContext context,
          final String typeName) throws UnableToCompleteException {

    final ErraiConfiguration erraiConfiguration = new ErraiAppPropertiesConfiguration();
    final String fullyQualifiedTypeName = distillTargetTypeName(typeName);
    final MetaClass type = MetaClassFactory.get(fullyQualifiedTypeName);
    final String className = MarshallerGeneratorFactory.getMarshallerImplClassName(type, true, erraiConfiguration);
    final MetaClass cachedType = cachedPortableTypes.get(fullyQualifiedTypeName);

    final PrintWriter printWriter = context.tryCreate(logger, PACKAGE_NAME, className);
    if (printWriter != null) {
      if (!RebindUtils.NO_CACHE && cachedType != null && cachedType.hashContent() == type.hashContent()) {
        log.debug("Reusing cached marshaller for {}", fullyQualifiedTypeName);
        printWriter.append(cachedSourceByTypeName.get(fullyQualifiedTypeName));
        context.commit(logger, printWriter);
      } else {
        log.debug("Generating marshaller for {}", fullyQualifiedTypeName);
        final String generatedSource = generateMarshaller(context, type, erraiConfiguration,
                MarshallersGenerator.getMetaClassFinder());
        printWriter.append(generatedSource);
        RebindUtils.writeStringToJavaSourceFileInErraiCacheDir(PACKAGE_NAME, className, generatedSource);
        context.commit(logger, printWriter);
        cachedPortableTypes.put(fullyQualifiedTypeName, type);
        cachedSourceByTypeName.put(fullyQualifiedTypeName, generatedSource);
      }

      return new RebindResult(RebindMode.USE_ALL_NEW, getMarshallerTypeName(className));
    } else {
      log.debug("Reusing existing marshaller for {}", fullyQualifiedTypeName);
      return new RebindResult(RebindMode.USE_EXISTING, getMarshallerTypeName(className));
    }
  }

  private String getMarshallerTypeName(final String className) {
    return PACKAGE_NAME + "." + className;
  }

  public String generateMarshaller(final GeneratorContext context,
          final MetaClass type,
          final ErraiConfiguration erraiConfiguration,
          final MetaClassFinder metaClassFinder) {

    final String className = MarshallerGeneratorFactory.getMarshallerImplClassName(type, true, erraiConfiguration);
    final String marshallerTypeName = getMarshallerTypeName(className);
    final MarshallerOutputTarget target = MarshallerOutputTarget.GWT;
    final GeneratorMappingContext generatorMappingContext = GeneratorMappingContextFactory.getFor(context, target);
    final MappingStrategy strategy = MappingStrategyFactory.createStrategy(true, generatorMappingContext, type, erraiConfiguration);

    if (type.isArray()) {
      return MarshallerGeneratorFactory.getFor(context, target, erraiConfiguration, metaClassFinder)
              .generateArrayMarshaller(type, marshallerTypeName, true)
              .toJavaString();
    }

    final ClassStructureBuilder<?> marshaller = strategy.getMapper().getMarshaller(marshallerTypeName);
    return marshaller.toJavaString();
  }

  private String distillTargetTypeName(final String marshallerName) {
    final int pos = marshallerName.lastIndexOf(MarshallerGeneratorFactory.MARSHALLER_NAME_PREFIX);
    String typeName = marshallerName.substring(pos).replace(MarshallerGeneratorFactory.MARSHALLER_NAME_PREFIX, "");

    final boolean isArrayType = typeName.startsWith(MarshallingGenUtil.ARRAY_VAR_PREFIX);
    typeName = StringUtils.replace(typeName, MarshallingGenUtil.ARRAY_VAR_PREFIX, "");
    typeName = StringUtils.replace(typeName, "_", ".");
    typeName = StringUtils.replace(typeName, MarshallingGenUtil.ERRAI_DOLLARSIGN_REPLACEMENT, "$");
    typeName = StringUtils.replace(typeName, MarshallingGenUtil.ERRAI_UNDERSCORE_REPLACEMENT, "_");

    if (isArrayType) {
      final int lastDot = typeName.lastIndexOf(".");
      final int dimension = Integer.parseInt(typeName.substring(lastDot + 2));
      typeName = typeName.substring(0, lastDot);

      final String primitiveName = AbstractMetaClass.getInternalPrimitiveNameFrom(typeName);
      final boolean isPrimitiveArrayType = !primitiveName.equals(typeName);

      typeName = "";
      for (int i = 0; i < dimension; i++) {
        typeName += "[";
      }
      if (!isPrimitiveArrayType) {
        typeName += "L";
      }
      typeName += primitiveName;
      if (!isPrimitiveArrayType) {
        typeName += ";";
      }
    }

    return typeName;
  }

  @Override
  public long getVersionId() {
    return GENERATOR_VERSION_ID;
  }

}
