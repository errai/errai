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

package org.jboss.errai.ioc.rebind.ioc.bootstrapper;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import org.jboss.errai.codegen.meta.impl.gwt.GWTUtil;
import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.config.rebind.AsyncCodeGenerator;
import org.jboss.errai.config.rebind.AsyncGenerationJob;
import org.jboss.errai.config.rebind.AsyncGenerators;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.config.rebind.GenerateAsync;
import org.jboss.errai.config.util.ThreadUtil;
import org.jboss.errai.ioc.client.Bootstrapper;

import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * The main generator class for the Errai IOC framework.
 * <p/>
 * <pre>
 *
 * </pre>
 *
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@GenerateAsync(Bootstrapper.class)
public class IOCGenerator extends Generator implements AsyncCodeGenerator {
  private final String className = Bootstrapper.class.getSimpleName() + "Impl";
  private final String packageName = Bootstrapper.class.getPackage().getName();
  private static final Object generatorLock = new Object();

  public static final boolean isTestMode = EnvUtil.isJUnitTest();

  public IOCGenerator() {
  }


  @Override
  public String generate(final TreeLogger logger,
                         final GeneratorContext context,
                         final String typeName)
      throws UnableToCompleteException {

    try {
      synchronized (generatorLock) {
        // Generate class source code
        final PrintWriter printWriter = context.tryCreate(logger, packageName, className);
        // if null, source code has ALREADY been generated,
        if (printWriter != null) {

          final Future<String> future = AsyncGenerationJob.createBuilder()
              .treeLogger(logger)
              .generatorContext(context)
              .interfaceType(Bootstrapper.class)
              .runIfStarting(new Runnable() {
                @Override
                public void run() {
                  GWTUtil.populateMetaClassFactoryFromTypeOracle(context, logger);
                }
              }).build().submit();

          final String csq = future.get();

          printWriter.append(csq);
          printWriter.flush();

          logger.log(TreeLogger.INFO, "generating ioc bootstrapping code...");

          context.commit(logger, printWriter);
        }
      }
    }
    catch (Throwable e) {
      // record sendNowWith logger that Map generation threw an exception
      e.printStackTrace();
      logger.log(TreeLogger.ERROR, "Error generating extensions", e);
    }
    // return the fully qualified name of the class generated

    String generatedClass = packageName + "." + className;
    return generatedClass;
  }

  @Override
  public Future<String> generateAsync(final TreeLogger logger, final GeneratorContext context) {
    // get print writer that receives the source code

    return ThreadUtil.submit(new Callable<String>() {
      @Override
      public String call() throws Exception {

        final Set<String> translatablePackages = RebindUtils.findTranslatablePackages(context);

        final IOCBootstrapGenerator iocBootstrapGenerator = new IOCBootstrapGenerator(context, logger,
            translatablePackages, false);

        final String out = iocBootstrapGenerator.generate(packageName, className);

        if (Boolean.getBoolean("errai.codegen.printOut")) {
          System.out.println("---IOC Bootstrapper--->");
          System.out.println(out);
          System.out.println("<--IOC bootstrapper---");
        }

        return out;
      }
    });
  }

}