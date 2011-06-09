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

package org.jboss.errai.workspaces.rebind;


import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import org.jboss.errai.bus.rebind.ProcessingContext;
import org.jboss.errai.bus.rebind.ScannerSingleton;
import org.jboss.errai.bus.server.annotations.security.RequireRoles;
import org.jboss.errai.bus.server.service.metadata.MetaDataScanner;
import org.jboss.errai.ioc.rebind.IOCGenerator;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.workspaces.client.api.ProvisioningCallback;
import org.jboss.errai.workspaces.client.api.WidgetProvider;
import org.jboss.errai.workspaces.client.api.annotations.GroupOrder;
import org.jboss.errai.workspaces.client.api.annotations.LoadTool;
import org.jboss.errai.workspaces.client.api.annotations.LoadToolSet;
import org.jboss.errai.workspaces.client.api.annotations.LoginComponent;

import java.io.*;
import java.util.*;

public class WorkspaceLoaderBootstrapGenerator extends Generator {
  /**
   * Simple name of class to be generated
   */
  private String className = null;

  /**
   * Package name of class to be generated
   */
  private String packageName = null;

  private final static String TOOLSET_PROFILE = "toolset-profile.properties";

  private TypeOracle typeOracle;
  private ProcessingContext ctx;
  private IOCGenerator iocGenerator;

  private volatile int counter = 0;

  // inherited generator method

  public String generate(TreeLogger logger, GeneratorContext context,
                         String typeName) throws UnableToCompleteException {

    typeOracle = context.getTypeOracle();

    try {
      // get classType and save instance variables

      JClassType classType = typeOracle.getType(typeName);
      packageName = classType.getPackage().getName();
      className = classType.getSimpleSourceName() + "Impl";

      // Generate class source code
      generateClass(logger, context);

    }
    catch (Exception e) {

      // record sendNowWith logger that Map generation threw an exception
      logger.log(TreeLogger.ERROR, "Error generating bootstrap loader", e);

    }

    // return the fully qualifed name of the class generated
    return packageName + "." + className;
  }

  /**
   * Generate source code for new class. Class extends
   * <code>HashMap</code>.
   *
   * @param logger  Logger object
   * @param context Generator context
   */
  private void generateClass(TreeLogger logger, GeneratorContext context) {

    // get print writer that receives the source code
    PrintWriter printWriter = context.tryCreate(logger, packageName, className);
    // print writer if null, source code has ALREADY been generated,

    if (printWriter == null) return;

    // init composer, set class properties, create source writer
    ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(packageName,
        className);

    composer.addImplementedInterface("org.jboss.errai.workspaces.client.framework.WorkspaceConfig");

    SourceWriter sourceWriter = composer.createSourceWriter(context, printWriter);

    ctx = new ProcessingContext(logger, context, sourceWriter, typeOracle);
    iocGenerator = new IOCGenerator(ctx);
    iocGenerator.initializeProviders(context, logger, sourceWriter);

    // generator constructor source code
    generateBootstrapClass(context, logger, sourceWriter);
    // close generated class
    sourceWriter.outdent();
    sourceWriter.println("}");

    // commit generated class
    context.commit(logger, printWriter);
  }

  private void generateBootstrapClass(final GeneratorContext context, final TreeLogger logger, final SourceWriter sourceWriter) {
    ResourceBundle bundle;

    try {
      bundle = ResourceBundle.getBundle("org.jboss.errai.workspaces.rebind.WorkspaceModules");
    }
    catch (Exception e) {
      logger.log(TreeLogger.Type.ERROR, "can't find WorkspaceModules.properties in classpath");
      logger.log(TreeLogger.Type.ERROR, e.getMessage());
      throw new RuntimeException();
    }

    // start constructor source generation
    sourceWriter.println("public " + className + "() { ");
    sourceWriter.indent();
    sourceWriter.println("super();");
    sourceWriter.outdent();
    sourceWriter.println("}");

    sourceWriter.println("public void configure(org.jboss.errai.workspaces.client.framework.ToolContainer workspace) { ");
    sourceWriter.outdent();

    // toolset profile (acts as whitelist). Used with BPM console atm
    final List<String> enabledTools = new ArrayList<String>();

    InputStream in = getClass().getClassLoader().getResourceAsStream(TOOLSET_PROFILE);

    if (in != null) {
      try {
        BufferedReader input = new BufferedReader(new InputStreamReader(in));
        try {
          String line = null;
          while ((line = input.readLine()) != null) {
            // ignore comments and empty lines
            if (line.equals("") || line.startsWith("#"))
              continue;

            enabledTools.add(line);
          }
        }
        finally {
          input.close();
        }
      }
      catch (IOException ex) {
        throw new RuntimeException("Error reading '" + TOOLSET_PROFILE + "'");
      }
    }

    for (Enumeration<String> keys = bundle.getKeys();
         keys.hasMoreElements(); ) {
      String key = keys.nextElement();

      sourceWriter.println("new " + bundle.getString(key) + "().initModule(errai);");
    }

    iocGenerator.generateAllProviders();

    final boolean applyFilter = in != null;
    MetaDataScanner scanner = ScannerSingleton.getOrCreateInstance();

    /**
     * LoadToolSet
     */
    Set<Class<?>> toolsets = scanner.getTypesAnnotatedWith(LoadToolSet.class);
    for (Class<?> toolSetClass : toolsets) {
      MetaClass clazz = MetaClassFactory.get(typeOracle, toolSetClass);

      if ((!applyFilter || enabledTools.contains(clazz.getFullyQualifedName()))) {
        iocGenerator.addType(clazz);
        String instance = iocGenerator.generateInjectors(clazz);
        sourceWriter.println("workspace.addToolSet(" + instance + ");");
        logger.log(TreeLogger.Type.INFO, "Adding Errai Toolset: " + clazz.getFullyQualifedName());
      }
    }

    /**
     * LoadTool
     */
    Set<Class<?>> tools = scanner.getTypesAnnotatedWith(LoadTool.class);
    for (Class<?> toolClass : tools) {
      MetaClass clazz = MetaClassFactory.get(typeOracle, toolClass);

      if ((!applyFilter || enabledTools.contains(clazz.getFullyQualifedName()))) {

        iocGenerator.addType(clazz);
        LoadTool loadTool = clazz.getAnnotation(LoadTool.class);

        logger.log(TreeLogger.Type.INFO, "Adding Errai Tool: " + clazz.getFullyQualifedName());

        if (clazz.isAnnotationPresent(RequireRoles.class)) {
          RequireRoles requireRoles = clazz.getAnnotation(RequireRoles.class);

          StringBuilder rolesBuilder = new StringBuilder("new String[] {");
          String[] roles = requireRoles.value();

          for (int i = 0; i < roles.length; i++) {
            rolesBuilder.append("\"").append(roles[i].trim()).append("\"");
            if ((i + 1) < roles.length) rolesBuilder.append(", ");
          }
          rolesBuilder.append("}");

          generateWidgetProvisioning(context, clazz.getFullyQualifedName(), loadTool, rolesBuilder, logger, sourceWriter);

        }
        else {
          generateWidgetProvisioning(context, clazz.getFullyQualifedName(), loadTool, null, logger, sourceWriter);

        }
      }
      else if (clazz.isAnnotationPresent(LoginComponent.class)) {
        sourceWriter.println("workspace.setLoginComponent(new " + clazz.getFullyQualifedName() + "());");
      }
    }

    /**
     * Group order
     */
    Set<Class<?>> groupOrderClasses = scanner.getTypesAnnotatedWith(GroupOrder.class);
    for (Class<?> clazz : groupOrderClasses) {
      GroupOrder groupOrder = clazz.getAnnotation(GroupOrder.class);

      if ("".equals(groupOrder.value().trim())) return;

      String[] order = groupOrder.value().split(",");

      sourceWriter.print("workspace.setPreferredGroupOrdering(new String[] {");

      for (int i = 0; i < order.length; i++) {
        sourceWriter.print("\"");
        sourceWriter.print(order[i].trim());
        sourceWriter.print("\"");

        if (i + 1 < order.length) {
          sourceWriter.print(",");
        }
      }

      sourceWriter.println("});");
    }

    // wrap up
    sourceWriter.outdent();
    sourceWriter.println("}");
  }

  public void generateWidgetProvisioning(final GeneratorContext context, String className, final LoadTool loadTool, final StringBuilder rolesBuilder, final TreeLogger logger, final SourceWriter writer) {
    MetaClass type;
    MetaClass widgetType;
    type = MetaClassFactory.get(typeOracle, className);
    widgetType = MetaClassFactory.get(typeOracle, Widget.class);

    if (widgetType == null) {
      throw new RuntimeException("error bootstrapping: " + className);
    }

    String providerName;

    if (widgetType.isAssignableFrom(type)) {


      writer.println(WidgetProvider.class.getName() + " widgetProvider" + (++counter) + " = new " + WidgetProvider.class.getName() + "() {");
      writer.outdent();
      writer.println("public void provideWidget(" + ProvisioningCallback.class.getName() + " callback) {");
      writer.outdent();
      String widgetName = iocGenerator
          .generateInjectors(type);

      writer.println("callback.onSuccess(" + widgetName + ");");
      writer.outdent();
      writer.println("}");
      writer.outdent();
      writer.println("};");

      providerName = "widgetProvider" + counter;
    }
    else {
      providerName = iocGenerator
          .generateInjectors(type);
    }

    writer.print("workspace.addTool(\"" + loadTool.group() + "\"," +
        " \"" + loadTool.name() + "\", \"" + loadTool.icon() + "\", " + loadTool.multipleAllowed()
        + ", " + loadTool.priority() + ",  " + providerName);

    if (rolesBuilder == null) {
      writer.println(");");
    }
    else {
      writer.println(", " + rolesBuilder.toString() + ");");
    }
  }
}


