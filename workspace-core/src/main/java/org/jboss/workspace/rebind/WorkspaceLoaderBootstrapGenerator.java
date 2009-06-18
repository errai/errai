package org.jboss.workspace.rebind;


import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.core.ext.typeinfo.JClassType;

import java.util.ResourceBundle;
import java.util.Enumeration;
import java.io.PrintWriter;

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

        composer.addImplementedInterface("org.jboss.workspace.client.framework.ModuleLoaderBootstrap");

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
            bundle = ResourceBundle.getBundle("org.jboss.workspace.rebind.WorkspaceModules");
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

        sourceWriter.println("public void initAll(org.jboss.workspace.client.layout.WorkspaceLayout workspace) { ");
        sourceWriter.outdent();

        // add statements to pub key/value pairs from the resrouce bundle
        for (Enumeration<String> keys = bundle.getKeys();
             keys.hasMoreElements();) {
            String key = keys.nextElement();

            sourceWriter.println("new " + bundle.getString(key) + "().initModule(workspace);");
        }

        // end constructor source generation
        sourceWriter.outdent();
        sourceWriter.println("}");
    }

}


