package org.jboss.errai.rebind;


import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import org.jboss.errai.client.framework.annotations.GroupOrder;
import org.jboss.errai.client.framework.annotations.LoadTool;
import org.jboss.errai.client.framework.annotations.LoadToolSet;
import org.jboss.errai.client.framework.annotations.LoginComponent;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.Thread.currentThread;
import java.net.URL;
import java.util.Enumeration;
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

            // record to logger that Map generation threw an exception
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

        composer.addImplementedInterface("org.jboss.errai.client.framework.ModuleLoaderBootstrap");

        SourceWriter sourceWriter = composer.createSourceWriter(context, printWriter);

        // generator constructor source code
        generateBootstrapClass(logger, sourceWriter);
        // close generated class
        sourceWriter.outdent();
        sourceWriter.println("}");

        // commit generated class
        context.commit(logger, printWriter);
    }

    private void generateBootstrapClass(TreeLogger logger, SourceWriter sourceWriter) {

        // init resource bundle

        ResourceBundle bundle;

        try {
            bundle = ResourceBundle.getBundle("org.jboss.errai.rebind.WorkspaceModules");
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

        sourceWriter.println("public void initAll(org.jboss.errai.client.layout.WorkspaceLayout errai) { ");
        sourceWriter.outdent();

        // add statements to pub key/value pairs from the resrouce bundle
        for (Enumeration<String> keys = bundle.getKeys();
             keys.hasMoreElements();) {
            String key = keys.nextElement();

            sourceWriter.println("new " + bundle.getString(key) + "().initModule(errai);");
        }

        try {
            Enumeration<URL> targets = currentThread().getContextClassLoader().getResources("ErraiApp.properties");

            while (targets.hasMoreElements()) {
                findLoadableModules(logger, sourceWriter, targets.nextElement());
            }
        }
        catch (IOException e) {
            logger.log(TreeLogger.Type.INFO, "no module loading roots found");
        }


        // end constructor source generation
        sourceWriter.outdent();
        sourceWriter.println("}");
    }


    private void findLoadableModules(TreeLogger logger, SourceWriter writer, URL url) {
        File root = new File(url.getFile()).getParentFile();
        _findLoadableModules(logger, writer, root, root);
    }

    private void _findLoadableModules(TreeLogger logger, SourceWriter writer, File root, File start) {
        for (File file : start.listFiles()) {
            if (file.isDirectory()) _findLoadableModules(logger, writer, root, file);
            if (file.getName().endsWith(".class")) {
                try {
                    String FQCN = getCandidateFQCN(root.getAbsolutePath(), file.getAbsolutePath());
                    Class clazz = Class.forName(FQCN);

                    if (clazz.isAnnotationPresent(LoadToolSet.class)) {
                        writer.println("org.jboss.errai.client.Workspace.addToolSet(new " + clazz.getName() + "());");
                        logger.log(TreeLogger.Type.INFO, "Adding Errai Toolset: " + clazz.getName());
                    }
                    else if (clazz.isAnnotationPresent(LoadTool.class)) {
                        LoadTool loadTool = (LoadTool) clazz.getAnnotation(LoadTool.class);

                        writer.println("org.jboss.errai.client.Workspace.addTool(\"" + loadTool.group() + "\"," +
                                " \"" + loadTool.name() + "\", \"" + loadTool.icon() + "\", " + loadTool.multipleAllowed()
                                + ", " + loadTool.priority() + ", new " + clazz.getName() + "());");
                    }
                    else if (clazz.isAnnotationPresent(LoginComponent.class)) {
                        writer.println("org.jboss.errai.client.Workspace.setLoginComponent(new " + clazz.getName() + "());");
                    }
                    else if (clazz.isAnnotationPresent(GroupOrder.class)) {
                        GroupOrder groupOrder = (GroupOrder) clazz.getAnnotation(GroupOrder.class);

                        if ("".equals(groupOrder.value().trim())) continue;

                        String[] order = groupOrder.value().split(",");

                        writer.print("org.jboss.errai.client.Workspace.setPreferredGroupOrdering(new String[] {");

                        for (int i = 0; i < order.length; i++) {
                            writer.print("\"");
                            writer.print(order[i].trim());
                            writer.print("\"");

                            if (i+1<order.length) {
                                writer.print(",");
                            }
                        }

                        writer.println("});");
                    }
                }
                catch (NoClassDefFoundError e) {
                    // do nothing.
                }
                catch (ExceptionInInitializerError e) {
                    // do nothing.
                }
                catch (UnsupportedOperationException e) {
                    // do nothing.
                }
                catch (ClassNotFoundException e) {
                    // do nothing.
                }
            }
        }
    }

    private String getCandidateFQCN(String rootFile, String fileName) {
        return fileName.replaceAll("(/|\\\\)", ".")
                .substring(rootFile.length() + 1, fileName.lastIndexOf('.'));
    }

}


