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
import org.jboss.errai.bus.server.annotations.security.RequireRoles;
import org.jboss.errai.bus.server.util.ConfigUtil;
import org.jboss.errai.bus.server.util.RebindVisitor;
import org.jboss.errai.workspaces.client.api.annotations.GroupOrder;
import org.jboss.errai.workspaces.client.api.annotations.LoadTool;
import org.jboss.errai.workspaces.client.api.annotations.LoadToolSet;
import org.jboss.errai.workspaces.client.api.annotations.LoginComponent;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

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

        // generator constructor source code
        generateBootstrapClass(context, logger, sourceWriter);
        // close generated class
        sourceWriter.outdent();
        sourceWriter.println("}");

        // commit generated class
        context.commit(logger, printWriter);
    }

    private void generateBootstrapClass(GeneratorContext context, TreeLogger logger, SourceWriter sourceWriter) {

        // init resource bundle

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
                //use buffering, reading one line at a time
                //FileReader always assumes default encoding is OK!
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
             keys.hasMoreElements();) {
            String key = keys.nextElement();

            sourceWriter.println("new " + bundle.getString(key) + "().initModule(errai);");
        }


        List<File> targets = ConfigUtil.findAllConfigTargets();

        final boolean applyFilter = in != null;

        ConfigUtil.visitAllTargets(
                targets, context, logger,
                sourceWriter,
                new RebindVisitor() {
                    public void visit(Class<?> clazz, GeneratorContext context, TreeLogger logger, SourceWriter writer) {

                        if (clazz.isAnnotationPresent(LoadToolSet.class)
                                && (!applyFilter || enabledTools.contains(clazz.getName()))) {
                            writer.println("workspace.addToolSet(new " + clazz.getName() + "());");
                            logger.log(TreeLogger.Type.INFO, "Adding Errai Toolset: " + clazz.getName());
                        } else if (clazz.isAnnotationPresent(LoadTool.class)
                                && (!applyFilter || enabledTools.contains(clazz.getName()))) {
                            LoadTool loadTool = clazz.getAnnotation(LoadTool.class);

                            if (clazz.isAnnotationPresent(RequireRoles.class)) {
                                RequireRoles requireRoles = clazz.getAnnotation(RequireRoles.class);

                                StringBuilder rolesBuilder = new StringBuilder("new String[] {");
                                String[] roles = requireRoles.value();

                                for (int i = 0; i < roles.length; i++) {
                                    rolesBuilder.append("\"").append(roles[i].trim()).append("\"");
                                    if ((i + 1) < roles.length) rolesBuilder.append(", ");
                                }
                                rolesBuilder.append("}");

                                writer.println("workspace.addTool(\"" + loadTool.group() + "\"," +
                                        " \"" + loadTool.name() + "\", \"" + loadTool.icon() + "\", " + loadTool.multipleAllowed()
                                        + ", " + loadTool.priority() + ", new " + clazz.getName() + "(), " + rolesBuilder.toString() + ");");
                            } else {
                                writer.println("workspace.addTool(\"" + loadTool.group() + "\"," +
                                        " \"" + loadTool.name() + "\", \"" + loadTool.icon() + "\", " + loadTool.multipleAllowed()
                                        + ", " + loadTool.priority() + ", new " + clazz.getName() + "());");
                            }
                        } else if (clazz.isAnnotationPresent(LoginComponent.class)) {
                            writer.println("workspace.setLoginComponent(new " + clazz.getName() + "());");
                        }

                        if (clazz.isAnnotationPresent(GroupOrder.class)) {
                            GroupOrder groupOrder = clazz.getAnnotation(GroupOrder.class);

                            if ("".equals(groupOrder.value().trim())) return;

                            String[] order = groupOrder.value().split(",");

                            writer.print("workspace.setPreferredGroupOrdering(new String[] {");

                            for (int i = 0; i < order.length; i++) {
                                writer.print("\"");
                                writer.print(order[i].trim());
                                writer.print("\"");

                                if (i + 1 < order.length) {
                                    writer.print(",");
                                }
                            }

                            writer.println("});");
                        }
                    }
                });


        sourceWriter.outdent();
        sourceWriter.println("}");
    }


}


