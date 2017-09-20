/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ioc.rebind.ioc.bootstrapper;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.server.api.ErraiConfig;
import org.jboss.errai.config.ErraiAppPropertiesConfiguration;
import org.jboss.errai.config.ErraiConfiguration;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.ioc.client.container.ClientBeanManager;
import org.jboss.errai.ioc.client.container.IOCEnvironment;
import org.jboss.errai.ioc.client.container.SyncBeanManagerImpl;
import org.jboss.errai.ioc.client.container.async.AsyncBeanManagerImpl;

import java.io.PrintWriter;

import static org.jboss.errai.config.ErraiAppPropertiesErraiAppConfiguration.ERRAI_IOC_ASYNC_BEAN_MANAGER;

/**
 * @author Mike Brock
 */
public class IOCEnvironmentGenerator extends Generator {

  public static final String PACKAGE_NAME = "org.jboss.errai.ioc.client.container";
  public static final String CLASS_NAME = "IOCEnvironmentImpl";

  @Override
  public String generate(final TreeLogger logger,
                         final GeneratorContext context,
                         final String typeName) throws UnableToCompleteException {
    try {

      logger.log(TreeLogger.INFO, "Generating Extensions Bootstrapper...");

      // Generate class source code
      generateIOCEnvironment(logger, context);

      // return the fully qualified name of the class generated
      return PACKAGE_NAME + "." + CLASS_NAME;
    }
    catch (Throwable e) {
      // record sendNowWith logger that Map generation threw an exception
      e.printStackTrace();
      logger.log(TreeLogger.ERROR, "Error generating extensions", e);
      throw new RuntimeException("error generating", e);
    }
  }

  private void generateIOCEnvironment(final TreeLogger logger, final GeneratorContext generatorContext) {

    final PrintWriter printWriter = generatorContext.tryCreate(logger, PACKAGE_NAME, CLASS_NAME);
    if (printWriter == null) {
      return;
    }

    final String csq = generate(new ErraiAppPropertiesConfiguration());

    printWriter.append(csq);
    generatorContext.commit(logger, printWriter);
  }

  public String generate(final ErraiConfiguration erraiConfiguration) {
    final boolean asyncBootstrap = erraiConfiguration.app().asyncBeanManager();
    final Statement newBeanManager = asyncBootstrap ? Stmt.newObject(AsyncBeanManagerImpl.class) : Stmt.newObject(SyncBeanManagerImpl.class);

    final ClassStructureBuilder<? extends ClassStructureBuilder<?>> builder
        = ClassBuilder.define(PACKAGE_NAME + "." + CLASS_NAME).publicScope()
        .implementsInterface(IOCEnvironment.class)
        .body()
        .publicMethod(boolean.class, "isAsync")
        .append(Stmt.load(asyncBootstrap).returnValue())
        .finish()
        .publicMethod(ClientBeanManager.class, "getNewBeanManager")
        .append(Stmt.nestedCall(newBeanManager).returnValue())
        .finish();

    return builder.toJavaString();
  }
}
