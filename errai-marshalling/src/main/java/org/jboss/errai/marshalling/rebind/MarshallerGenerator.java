/*
 * Copyright 2014 JBoss, by Red Hat, Inc
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

package org.jboss.errai.marshalling.rebind;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.impl.AbstractMetaClass;
import org.jboss.errai.codegen.meta.impl.build.BuildMetaClass;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.marshalling.client.api.MarshallerFramework;
import org.jboss.errai.marshalling.rebind.api.GeneratorMappingContextFactory;
import org.jboss.errai.marshalling.rebind.api.MappingStrategy;
import org.jboss.errai.marshalling.rebind.util.MarshallingGenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.IncrementalGenerator;
import com.google.gwt.core.ext.RebindMode;
import com.google.gwt.core.ext.RebindResult;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

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
  private final String packageName = MarshallerFramework.class.getPackage().getName();
  
  // We're keeping this cache of portable types to compare their contents and
  // find out if they have changed since the last refresh.
  private static Map<String, MetaClass> cachedPortableTypes = new ConcurrentHashMap<String, MetaClass>();
  
  /*
   * A version id. Increment this as needed, when structural changes are made to
   * the generated output, specifically with respect to it's effect on the
   * caching and reuse of previous generator results. Previously cached
   * generator results will be invalidated automatically if they were generated
   * by a version of this generator with a different version id.
   */
  private static final long GENERATOR_VERSION_ID = 1L;

  @Override
  public RebindResult generateIncrementally(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
    String fullyQualifiedTypeName = distillTargetTypeName(typeName);
    MetaClass type = MetaClassFactory.get(fullyQualifiedTypeName);
    String className = MarshallerGeneratorFactory.MARSHALLER_NAME_PREFIX + MarshallingGenUtil.getVarName(type) + "_Impl";
    String marshallerTypeName = packageName + "." + className;
    
    MetaClass cachedType = cachedPortableTypes.get(fullyQualifiedTypeName);
    if (cachedType != null && cachedType.hashContent() == type.hashContent()) {
      log.debug("Reusing cached marshaller for "  + fullyQualifiedTypeName);
      return new RebindResult(RebindMode.USE_ALL_CACHED, marshallerTypeName);
    }
    
    log.debug("Generating marshaller for "  + fullyQualifiedTypeName);
    final PrintWriter printWriter = context.tryCreate(logger, packageName, className);
    if (printWriter != null) {
      generateMarshaller(context, type, className, marshallerTypeName, logger, printWriter);
      cachedPortableTypes.put(fullyQualifiedTypeName, type);
      return new RebindResult(RebindMode.USE_ALL_NEW, marshallerTypeName);
    }
    else {
      // This should never happen as the generated marshaller factory keeps a
      // map of marshaller instances. Therefore, GWT.create should never be
      // invoked twice for the same marshaller.
      return new RebindResult(RebindMode.USE_EXISTING, marshallerTypeName);
    }
  }

  private void generateMarshaller(final GeneratorContext context, final MetaClass type, final String className,
          final String marshallerTypeName, final TreeLogger logger, final PrintWriter printWriter) {
    
    MarshallerOutputTarget target = MarshallerOutputTarget.GWT;
    final MappingStrategy strategy =
        MappingStrategyFactory.createStrategy(true, GeneratorMappingContextFactory.getFor(context, target), type);

    String gen = null;
    if (type.isArray()) {
      BuildMetaClass marshallerClass =
          MarshallerGeneratorFactory.generateArrayMarshaller(type, marshallerTypeName, true);
      gen = marshallerClass.toJavaString();
    }
    else {
      final ClassStructureBuilder<?> marshaller = strategy.getMapper().getMarshaller(marshallerTypeName);
      gen = marshaller.toJavaString();
    }
    printWriter.append(gen);

    final File tmpFile = new File(RebindUtils.getErraiCacheDir().getAbsolutePath() + "/" + className + ".java");
    RebindUtils.writeStringToFile(tmpFile, gen);
    
    context.commit(logger, printWriter);
  }

  private String distillTargetTypeName(String marshallerName) {
    int pos = marshallerName.lastIndexOf(MarshallerGeneratorFactory.MARSHALLER_NAME_PREFIX);
    String typeName = marshallerName.substring(pos).replace(MarshallerGeneratorFactory.MARSHALLER_NAME_PREFIX, "");

    boolean isArrayType = typeName.startsWith(MarshallingGenUtil.ARRAY_VAR_PREFIX);
    typeName = StringUtils.replace(typeName, MarshallingGenUtil.ARRAY_VAR_PREFIX, "");
    typeName = StringUtils.replace(typeName, "_", ".");
    typeName = StringUtils.replace(typeName, MarshallingGenUtil.ERRAI_DOLLARSIGN_REPLACEMENT, "$");
    typeName = StringUtils.replace(typeName, MarshallingGenUtil.ERRAI_UNDERSCORE_REPLACEMENT, "_");

    if (isArrayType) {
      int lastDot = typeName.lastIndexOf(".");
      int dimension = Integer.parseInt(typeName.substring(lastDot + 2));
      typeName = typeName.substring(0, lastDot);

      String primitiveName = AbstractMetaClass.getInternalPrimitiveNameFrom(typeName);
      boolean isPrimitiveArrayType = !primitiveName.equals(typeName);

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