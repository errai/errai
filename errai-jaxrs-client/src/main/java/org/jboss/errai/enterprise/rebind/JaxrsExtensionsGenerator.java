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

import java.io.PrintWriter;

import javax.ws.rs.Path;

import org.jboss.errai.bus.client.framework.RemoteServiceProxyFactory;
import org.jboss.errai.bus.rebind.ScannerSingleton;
import org.jboss.errai.bus.server.service.metadata.MetaDataScanner;
import org.jboss.errai.enterprise.client.jaxrs.JaxrsExtensionsLoader;
import org.jboss.errai.ioc.rebind.ioc.codegen.InnerClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.MethodBlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.Stmt;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

/**
 * Generates the JAX-RS extensions (remote proxies, serializers).
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JaxrsExtensionsGenerator extends Generator {
  
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
        logger.log(TreeLogger.ERROR, typeName + "is not an inteface.");
        throw new RuntimeException("Invalid type");
      }
      
      packageName = classType.getPackage().getName();
      className = classType.getSimpleSourceName() + "Impl";

      logger.log(TreeLogger.INFO, "Generating JAX-RS Extensions...");

      generateClass(logger, context);
    }
    catch (Throwable e) {
      logger.log(TreeLogger.ERROR, "Error generating JAX-RS extensions", e);
    }

    return packageName + "." + className;
  }

  private void generateClass(TreeLogger logger, GeneratorContext context) {
    MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();
    
    PrintWriter printWriter = context.tryCreate(logger, packageName, className);
    // Code has already been generated.
    if (printWriter == null)
      return;
   
    ClassStructureBuilder<?> classBuilder = ClassBuilder.implement(JaxrsExtensionsLoader.class);
    MethodBlockBuilder<?> createProxies = classBuilder.publicMethod(void.class, "createProxies");
    
    for (Class<?> remote : scanner.getTypesAnnotatedWith(Path.class)) {
      // create the remote proxy for this interface
      ClassStructureBuilder<?> remoteProxy = new JaxrsProxyGenerator(remote).generate();
      createProxies.append(new InnerClass(remoteProxy.getClassDefinition()));
      
      createProxies.append(Stmt.invokeStatic(RemoteServiceProxyFactory.class, "addRemoteProxy", 
          remote, Stmt.newObject(classBuilder.getClassDefinition())));
    }
    classBuilder = (ClassStructureBuilder<?>) createProxies.finish();
    
    printWriter.append(classBuilder.toJavaString());
    context.commit(logger, printWriter);
  }
}