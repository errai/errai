/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import org.jboss.errai.bus.server.util.ConfigUtil;
import org.jboss.errai.bus.server.util.RebindVisitor;
import org.jboss.errai.workspaces.client.framework.annotations.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IconFactoryGenerator extends Generator {
  /**
   * Simple name of class to be generated
   */
  private String className = null;

  /**
   * Package name of class to be generated
   */
  private String packageName = null;

  private Class bundleClass = null;
  private Map<String, String> tool2imageRes = new HashMap<String,String>();

  // inherited generator method
  public String generate(TreeLogger logger, GeneratorContext context,
                         String typeName) throws UnableToCompleteException {

    TypeOracle typeOracle = context.getTypeOracle();

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
      logger.log(TreeLogger.ERROR, "Error generating icon factory", e);

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

    composer.addImport("java.util.HashMap");
    composer.addImport("java.util.Map");
    composer.addImport("org.jboss.errai.workspaces.client.framework.IconFactory");
    composer.addImport("com.google.gwt.core.client.GWT");
    composer.addImport("com.google.gwt.resources.client.ImageResource");

    composer.addImplementedInterface("org.jboss.errai.workspaces.client.framework.IconFactory");

    SourceWriter sourceWriter = composer.createSourceWriter(context, printWriter);

    // parse classes
    List<File> targets = ConfigUtil.findAllConfigTargets();
    ConfigUtil.visitAllTargets(
        targets, context, logger,
        sourceWriter,
        new RebindVisitor()
        {
          public void visit(Class<?> clazz, GeneratorContext context, TreeLogger logger, SourceWriter writer)
          {
            if (clazz.isAnnotationPresent(DefaultBundle.class)) {
              bundleClass = clazz.getAnnotation(DefaultBundle.class).value();
            }
            else if(clazz.isAnnotationPresent(LoadTool.class))
            {
              LoadTool lt = clazz.getAnnotation(LoadTool.class);
              if(!"".equals(lt.icon()))
                tool2imageRes.put(lt.name(), lt.icon());
            }
          }
        }
    );


    // generator constructor source code
    generateFactoryClass(context, logger, sourceWriter);

    // close generated class
    sourceWriter.outdent();
    sourceWriter.println("}");

    // commit generated class
    context.commit(logger, printWriter);
  }

  private void generateFactoryClass(
      GeneratorContext context,
      TreeLogger logger,
      SourceWriter sourceWriter)
  {

    sourceWriter.println("private Map<String,ImageResource> mapping = new HashMap<String,ImageResource>();");
    
    // start constructor source generation
    sourceWriter.println("public " + className + "() { ");
    sourceWriter.indent();
    sourceWriter.println("super();");

    sourceWriter.println(bundleClass.getName() + " bundle = ("+bundleClass.getName()+") GWT.create("+bundleClass.getName()+".class);");
    for(String tool : tool2imageRes.keySet())
    {
      sourceWriter.println("mapping.put(\""+tool+"\", bundle."+tool2imageRes.get(tool)+"() );");
    }
    sourceWriter.outdent();
    sourceWriter.println("}");

    sourceWriter.println("public ImageResource createIcon(String name) { ");
    sourceWriter.outdent();
    sourceWriter.println("    return mapping.get(name);");
    sourceWriter.outdent();
    sourceWriter.println("}");
        
  }


}


