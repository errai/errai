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

package org.jboss.errai.marshalling.rebind;

import java.io.File;
import java.io.PrintWriter;

import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.marshalling.client.api.MarshallerFactory;
import org.jboss.errai.marshalling.client.api.MarshallerFramework;
import org.jboss.errai.marshalling.rebind.api.GeneratorMappingContextFactory;
import org.jboss.errai.marshalling.rebind.api.MappingStrategy;
import org.jboss.errai.marshalling.rebind.util.MarshallingGenUtil;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

/**
 * Generator for a single marshaller (used for custom portable types).
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class MarshallerGenerator extends Generator {
  private final String packageName = MarshallerFramework.class.getPackage().getName();

  // TODO use incremental generator
  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
    String fqcnOfCustomType = typeName.replace(MarshallerFactory.class.getName() + "Impl", "")
        .replace("." + MarshallerGeneratorFactory.MARSHALLER_NAME_PREFIX, "")
        .replaceAll("_", ".");

    MetaClass type = MetaClassFactory.get(fqcnOfCustomType);
    String marshallerClassName =
        MarshallerGeneratorFactory.MARSHALLER_NAME_PREFIX + MarshallingGenUtil.getVarName(type) + "Impl";
    
    MarshallerOutputTarget target = MarshallerOutputTarget.GWT;
    final MappingStrategy strategy = MappingStrategyFactory
          .createStrategy(true, GeneratorMappingContextFactory.getFor(target), type);
    
    if (strategy == null) {
      throw new RuntimeException("no available marshaller for class: " + type.getFullyQualifiedName());
    }
    
    final ClassStructureBuilder<?> marshaller = strategy.getMapper().getMarshaller(packageName + "." + marshallerClassName);

//    if (type.isAnnotationPresent(AlwaysQualify.class)) {
//      constructor.append(loadVariable(varName).assignValue(
//            Stmt.newObject(QualifyingMarshallerWrapper.class, marshaller, type)));
//    }
//    else {
//      constructor.append(loadVariable(varName).assignValue(marshaller));
//    }
    
    final String gen = marshaller.toJavaString();
    final PrintWriter printWriter = context.tryCreate(logger, packageName, marshallerClassName);
    printWriter.append(gen);

    final File tmpFile = new File(RebindUtils.getErraiCacheDir().getAbsolutePath() + "/" + marshallerClassName + ".java");
    RebindUtils.writeStringToFile(tmpFile, gen);

    context.commit(logger, printWriter);
    
    return packageName + "." + marshallerClassName;
    
  }
}