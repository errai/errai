/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.ioc.rebind;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.cfg.ModuleDef;
import com.google.gwt.dev.javac.StandardGeneratorContext;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCBootstrapGenerator;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;

import java.io.PrintWriter;
import java.lang.reflect.Field;

/**
 * The main generator class for the errai-ioc framework.
 */
public class IOCGenerator extends Generator {
  /**
   * Simple name of class to be generated
   */
  private String className = null;

  /**
   * Package name of class to be generated
   */
  private String packageName = null;
  private TypeOracle typeOracle;

  private String modulePackage;

  public static final boolean isDebugCompile = Boolean.getBoolean("errai.ioc.debug");

  public IOCGenerator() {
  }

  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName)
          throws UnableToCompleteException {
    typeOracle = context.getTypeOracle();


    /**
     * Try to determine the module package -- hackishly
     */
    //TODO: Find a more standard way to do this.
    try {
      if (context instanceof StandardGeneratorContext) {
        StandardGeneratorContext stdContext = (StandardGeneratorContext) context;
        Field field = StandardGeneratorContext.class.getDeclaredField("module");
        field.setAccessible(true);

        ModuleDef moduleDef = (ModuleDef) field.get(stdContext);

        String moduleName = moduleDef.getName();

        for (int i = 0; i < moduleName.length(); i++) {
          if (moduleName.charAt(i) == '.' && i < moduleName.length()
                  && Character.isUpperCase(moduleName.charAt(i + 1))) {
            this.modulePackage = moduleName.substring(0, i);
            break;
          }
        }

        logger.log(TreeLogger.INFO, "will scan in package: " + modulePackage);
      }
    }
    catch (Exception e) { 
      throw new RuntimeException("could not determine module package", e);
      // could not determine package.
    }

    try {
      // get classType and save instance variables

      JClassType classType = typeOracle.getType(typeName);
      packageName = classType.getPackage().getName();
      className = classType.getSimpleSourceName() + "Impl";

      logger.log(TreeLogger.INFO, "Generating Extensions Bootstrapper...");

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
   * Generate source code for new class. Class extends
   * <code>HashMap</code>.
   *
   * @param logger  Logger object
   * @param context Generator context
   */
  private void generateIOCBootstrapClass(TreeLogger logger, GeneratorContext context) {
    // get print writer that receives the source code
    PrintWriter printWriter = context.tryCreate(logger, packageName, className);
    // print writer if null, source code has ALREADY been generated,

    if (printWriter == null) return;

    IOCBootstrapGenerator iocBootstrapGenerator = new IOCBootstrapGenerator(typeOracle, context, logger);
    if (modulePackage != null && modulePackage.length() != 0) {
      iocBootstrapGenerator.setPackageFilter(modulePackage);
    }


    printWriter.append(iocBootstrapGenerator.generate(packageName, className));
    // commit generated class
    context.commit(logger, printWriter);
  }

  public MetaClass getJClassType(Class cls) {
    try {
      return MetaClassFactory.get(typeOracle.getType(cls.getName()));
    }
    catch (NotFoundException e) {
      return null;
    }
  }
}
