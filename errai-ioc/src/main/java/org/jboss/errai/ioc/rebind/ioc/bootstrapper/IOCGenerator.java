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
import org.jboss.errai.config.rebind.EnvUtil;

import java.io.PrintWriter;

/**
 * The main generator class for the Errai IOC framework.
 *
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class IOCGenerator extends Generator {
  private String className = null;
  private String packageName = null;

  public static final boolean isTestMode = EnvUtil.isJUnitTest();

  public IOCGenerator() {
  }

  @Override
  public String generate(final TreeLogger logger,
                         final GeneratorContext context,
                         final String typeName)
          throws UnableToCompleteException {

    try {
      // get classType and save instance variables

      final JClassType classType = context.getTypeOracle().getType(typeName);
      packageName = classType.getPackage().getName();
      className = classType.getSimpleSourceName() + "Impl";

      logger.log(TreeLogger.INFO, "Generating Extensions Bootstrapper...");

      GWTUtil.populateMetaClassFactoryFromTypeOracle(context, logger);

      // Generate class source code
      generateIOCBootstrapClass(logger, context);
    }
    catch (Throwable e) {
      // record sendNowWith logger that Map generation threw an exception
      e.printStackTrace();
      logger.log(TreeLogger.ERROR, "Error generating extensions", e);
    }

    // return the fully qualified name of the class generated
    return packageName + "." + className;
  }

  /**
   * Generate source code for new class. Class extends <code>HashMap</code>.
   *
   * @param logger
   *         Logger object
   * @param context
   *         Generator context
   */
  private void generateIOCBootstrapClass(final TreeLogger logger, final GeneratorContext context) {
    // get print writer that receives the source code
    final PrintWriter printWriter = context.tryCreate(logger, packageName, className);

    // if null, source code has ALREADY been generated,
    if (printWriter == null)
      return;

    final IOCBootstrapGenerator iocBootstrapGenerator = new IOCBootstrapGenerator(context, logger,
            RebindUtils.findTranslatablePackages(context), false);

    final String out = iocBootstrapGenerator.generate(packageName, className);

    if (Boolean.getBoolean("errai.codegen.printOut")) {
      System.out.println("---IOC Bootstrapper--->");
      System.out.println(out);
      System.out.println("<--IOC bootstrapper---");
    }

    printWriter.append(out);
    context.commit(logger, printWriter);
  }
}