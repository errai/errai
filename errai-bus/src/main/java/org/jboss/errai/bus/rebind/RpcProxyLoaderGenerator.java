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

package org.jboss.errai.bus.rebind;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.ProxyProvider;
import org.jboss.errai.bus.client.framework.RemoteServiceProxyFactory;
import org.jboss.errai.bus.client.framework.RpcProxyLoader;
import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.MethodBlockBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.util.ClassScanner;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.metadata.RebindUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;

/**
 * Generates the implementation of {@link RpcProxyLoader}.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class RpcProxyLoaderGenerator extends Generator {
  private final Logger log = LoggerFactory.getLogger(RpcProxyLoaderGenerator.class);

  /**
   * Simple name of class to be generated
   */
  private String className = null;

  /**
   * Package name of class to be generated
   */
  private String packageName = null;

  private TypeOracle typeOracle;

  @Override
  public String generate(final TreeLogger logger, final GeneratorContext context, final String typeName)
          throws UnableToCompleteException {

    typeOracle = context.getTypeOracle();

    try {
      final JClassType classType = typeOracle.getType(typeName);
      packageName = classType.getPackage().getName();
      className = classType.getSimpleSourceName() + "Impl";

      final PrintWriter printWriter = context.tryCreate(logger, packageName, className);
      // If code has not already been generated.
      if (printWriter != null) {
        printWriter.append(generate(className));
        context.commit(logger, printWriter);
      }
    }
    catch (Throwable e) {
      logger.log(TreeLogger.ERROR, "Error generating extensions", e);
    }

    // return the fully qualified name of the class generated
    return packageName + "." + className;
  }

  private String generate(String className) {
    final File fileCacheDir = RebindUtils.getErraiCacheDir();
    final File cacheFile = new File(fileCacheDir.getAbsolutePath() + "/" + className + ".java");
    log.info("generating rpc proxy loader class.");

    String gen = generate();
    RebindUtils.writeStringToFile(cacheFile, gen);

    return gen;
  }

  private String generate() {
    ClassStructureBuilder<?> classBuilder = ClassBuilder.implement(RpcProxyLoader.class);

    final MethodBlockBuilder<?> loadProxies =
            classBuilder.publicMethod(void.class, "loadProxies", Parameter.of(MessageBus.class, "bus", true));

    for (MetaClass remote : ClassScanner.getTypesAnnotatedWith(Remote.class)) {
      if (remote.isInterface()) {
        // create the remote proxy for this interface
        final ClassStructureBuilder<?> remoteProxy = new RpcProxyGenerator(remote).generate();
        loadProxies.append(new InnerClass(remoteProxy.getClassDefinition()));

        // create the proxy provider
        final Statement proxyProvider = ObjectBuilder.newInstanceOf(ProxyProvider.class)
                .extend()
                .publicOverridesMethod("getProxy")
                .append(Stmt.nestedCall(Stmt.newObject(remoteProxy.getClassDefinition())).returnValue())
                .finish()
                .finish();

        loadProxies.append(Stmt.invokeStatic(RemoteServiceProxyFactory.class, "addRemoteProxy", remote, proxyProvider));
      }
    }

    classBuilder = (ClassStructureBuilder<?>) loadProxies.finish();
    final String generatedStr = classBuilder.toJavaString();
    if (Boolean.getBoolean("errai.codegen.printOut")) {
      System.out.println("----[start rpc proxy]---");
      System.out.println(generatedStr);
      System.out.println("----[end rpc proxy]-----");
    }
    return generatedStr;
  }
}