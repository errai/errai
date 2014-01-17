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

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

/**
 * Generator for a single marshaller used to lazily generate marshallers for custom portable types.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class MarshallerGenerator extends Generator {
  private final String packageName = MarshallerFramework.class.getPackage().getName();

  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
    MetaClass type = MetaClassFactory.get(distillTargetTypeName(typeName));
    
    MarshallerOutputTarget target = MarshallerOutputTarget.GWT;
    final MappingStrategy strategy = MappingStrategyFactory
          .createStrategy(true, GeneratorMappingContextFactory.getFor(target), type);

    String className = MarshallerGeneratorFactory.MARSHALLER_NAME_PREFIX + MarshallingGenUtil.getVarName(type) + "_Impl";
    String gen = null;
    if (type.isArray()) {
      BuildMetaClass marshallerClass = MarshallerGeneratorFactory.generateArrayMarshaller(type, packageName + "." + className);
      gen = marshallerClass.toJavaString();
    }
    else {
      final ClassStructureBuilder<?> marshaller =
          strategy.getMapper().getMarshaller(packageName + "." + className);
      gen = marshaller.toJavaString();
    }

    final PrintWriter printWriter = context.tryCreate(logger, packageName, className);
    printWriter.append(gen);

    final File tmpFile =
        new File(RebindUtils.getErraiCacheDir().getAbsolutePath() + "/" + className + ".java");
    RebindUtils.writeStringToFile(tmpFile, gen);

    context.commit(logger, printWriter);
    
    return packageName + "." + className;
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
}