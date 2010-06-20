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

package org.jboss.errai.bus.rebind;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.ext.ExtensionsLoader;
import org.jboss.errai.bus.server.annotations.ExtensionComponent;
import org.jboss.errai.bus.server.util.RebindVisitor;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import static org.jboss.errai.bus.server.util.ConfigUtil.*;

public class ExtensionProxyGenerator extends Generator {
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
    public String generate(TreeLogger logger, GeneratorContext context, String typeName)
            throws UnableToCompleteException {
        typeOracle = context.getTypeOracle();

        try {
            // get classType and save instance variables

            JClassType classType = typeOracle.getType(typeName);
            packageName = classType.getPackage().getName();
            className = classType.getSimpleSourceName() + "Impl";

            logger.log(TreeLogger.INFO, "Generating Extensions Bootstrapper...");

            // Generate class source code
            generateClass(logger, context);

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
    private void generateClass(TreeLogger logger, GeneratorContext context) {

        // get print writer that receives the source code
        PrintWriter printWriter = context.tryCreate(logger, packageName, className);
        // print writer if null, source code has ALREADY been generated,

        if (printWriter == null) return;

        // init composer, set class properties, create source writer
        ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(packageName,
                className);

        composer.addImplementedInterface(ExtensionsLoader.class.getName());

        SourceWriter sourceWriter = composer.createSourceWriter(context, printWriter);

        // generator constructor source code
        generateExtensions(context, logger, sourceWriter);
        // close generated class
        sourceWriter.outdent();
        sourceWriter.println("}");

     
        // commit generated class
        context.commit(logger, printWriter);
    }

    private void generateExtensions(GeneratorContext context, TreeLogger logger, SourceWriter sourceWriter) {
        // start constructor source generation
        sourceWriter.println("public " + className + "() { ");
        sourceWriter.indent();
        sourceWriter.println("super();");
        sourceWriter.outdent();
        sourceWriter.println("}");

        sourceWriter.println("public void initExtensions(final " + MessageBus.class.getName() + " bus) { ");
        sourceWriter.outdent();

        final List<File> targets = findAllConfigTargets();

        new BusClientConfigGenerator().generate(context, logger, sourceWriter, targets);
        
        visitAllTargets(targets, context, logger, sourceWriter,
                new RebindVisitor() {
                    public void visit(Class<?> visit, GeneratorContext context, TreeLogger logger, SourceWriter writer) {
                      //  System.out.println("Searching:" + visit);

                        if (isAnnotated(visit, ExtensionComponent.class, ExtensionGenerator.class)) {
                            try {
                                ExtensionGenerator generator = visit.asSubclass(ExtensionGenerator.class).newInstance();
                                generator.generate(context, logger, writer, targets);
                            }
                            catch (Exception e) {
                                throw new RuntimeException("Could not load extension generator: " + visit.getName(), e);
                            }
                        }
                    }

                    public void visitError(String className, Throwable t) {
                    }
                });

        // end constructor source generation
        sourceWriter.outdent();
        sourceWriter.println("}");
    }
}
