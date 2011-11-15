/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.enterprise.rebind;

import java.io.File;
import java.io.PrintWriter;

import javax.ws.rs.Path;

import org.jboss.errai.bus.client.framework.RemoteServiceProxyFactory;
import org.jboss.errai.codegen.framework.InnerClass;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.MethodBlockBuilder;
import org.jboss.errai.codegen.framework.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.framework.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.common.metadata.ScannerSingleton;
import org.jboss.errai.enterprise.client.jaxrs.JaxrsProxyLoader;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

/**
 * Generates the JAX-RS proxy loader.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsProxyLoaderGenerator extends Generator {
  private String className = null;
  private String packageName = null;

  private TypeOracle typeOracle;
  
  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName)
      throws UnableToCompleteException {
    typeOracle = context.getTypeOracle();

    try {
      JClassType classType = typeOracle.getType(typeName);

      if (classType.isInterface() == null) {
        logger.log(TreeLogger.ERROR, typeName + "is not an interface.");
        throw new RuntimeException("invalid type: not an interface");
      }

      packageName = classType.getPackage().getName();
      className = classType.getSimpleSourceName() + "Impl";

      PrintWriter printWriter = context.tryCreate(logger, packageName, className);
      // If code has not already been generated.
      if (printWriter != null) {
        printWriter.append(generate(logger, className));
        context.commit(logger, printWriter);
      }
    }
    catch (Throwable e) {
      logger.log(TreeLogger.ERROR, "Error generating JAX-RS extensions", e);
    }

    return packageName + "." + className;
  }
  
  private String generate(TreeLogger logger, String className) {
    File fileCacheDir = RebindUtils.getErraiCacheDir();
    File cacheFile = new File(fileCacheDir.getAbsolutePath() + "/" + className + ".java");
    
    String gen;
    if (!cacheFile.exists() || RebindUtils.hasClasspathChangedForAnnotatedWith(Path.class)) {
      logger.log(TreeLogger.INFO, "generating jax-rs proxy loader class.");
      gen = generate();
      RebindUtils.writeStringToFile(cacheFile, gen);
    } 
    else {
      logger.log(TreeLogger.INFO, "nothing has changed. using cached jax-rs proxy loader class.");
      gen = RebindUtils.readFileToString(cacheFile);
    }
    
    return gen;
  }
  
  private String generate() {
    MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();
    ClassStructureBuilder<?> classBuilder = ClassBuilder.implement(JaxrsProxyLoader.class);

    MethodBlockBuilder<?> loadProxies = classBuilder.publicMethod(void.class, "loadProxies");
    for (Class<?> remote : scanner.getTypesAnnotatedWith(Path.class, "")) {
      if (remote.isInterface()) {
        // create the remote proxy for this interface
        ClassStructureBuilder<?> remoteProxy = new JaxrsProxyGenerator(remote).generate();
        loadProxies.append(new InnerClass((BuildMetaClass) remoteProxy.getClassDefinition()));

        loadProxies.append(Stmt.invokeStatic(RemoteServiceProxyFactory.class, "addRemoteProxy",
            remote, Stmt.newObject(remoteProxy.getClassDefinition())));
      }
    }
    classBuilder = (ClassStructureBuilder<?>) loadProxies.finish();
    
    return classBuilder.toJavaString();
  }
}