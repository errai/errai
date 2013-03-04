/*
 * Copyright 2013 JBoss, by Red Hat, Inc
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

package org.jboss.errai.config.rebind;

import java.io.File;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.jboss.errai.common.metadata.RebindUtils;
import org.jboss.errai.config.util.ThreadUtil;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;

/**
 * Base class of all code generators that generate proxy loaders.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractProxyLoaderGenerator extends Generator implements AsyncCodeGenerator {

  @Override
  public Future<String> generateAsync(final TreeLogger logger, final GeneratorContext context) {
    return ThreadUtil.submit(new Callable<String>() {
      @Override
      public String call() throws Exception {
        return generate(context);
      }
    });
  }

  /**
   * Starts the asynchronous generators if they haven't been started yet and waits for the
   * asynchronous proxy loader generator to complete.
   * 
   * @param proxyLoader
   *          the proxy loader interface for which an implementation should be generated.
   * @param context
   *          the generation context to use.
   * @param logger
   *          the tree logger to use.
   * @param packageName
   *          the package name of the generated proxy loader.
   * @param className
   *          the class name of the generated proxy loader implementation.
   * 
   * @return the fully qualified class name of the generated proxy loader.
   */
  protected String generateProxyLoader(final Class<?> proxyLoader, final GeneratorContext context,
      final TreeLogger logger,
      final String packageName, final String className) {

    try {
      final PrintWriter printWriter = context.tryCreate(logger, packageName, className);
      if (printWriter != null) {
        final Future<String> future = AsyncGenerationJob.createBuilder()
            .treeLogger(logger)
            .generatorContext(context)
            .interfaceType(proxyLoader)
            .runIfStarting(new Runnable() {
              @Override
              public void run() {
                EnvUtil.populateMetaClassFactoryFromTypeOracle(context, logger);
              }
            })
            .build()
            // this causes all asynchronous code generators to run if this is the first one that executes.
            .submit();

        final String gen = future.get();
        printWriter.append(gen);

        final File tmpFile = new File(RebindUtils.getErraiCacheDir().getAbsolutePath() + "/" + className + ".java");
        RebindUtils.writeStringToFile(tmpFile, gen);

        context.commit(logger, printWriter);
      }
    }
    catch (Throwable e) {
      logger.log(TreeLogger.ERROR, "Error generating " + className, e);
    }

    return packageName + "." + className;
  }

  /**
   * Generates the actual proxy loader implementation as required by concrete implementations of
   * this class.
   * 
   * @param context
   *          the generator context to use.
   * 
   * @return the generated proxy loader implementation.
   */
  protected abstract String generate(final GeneratorContext context);

}