package org.jboss.errai.bus.rebind;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.ext.ExtensionsLoader;
import org.jboss.errai.bus.server.annotations.ExtensionComponent;
import org.jboss.errai.bus.server.util.ConfigUtil;
import org.jboss.errai.bus.server.util.RebindVisitor;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

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
    public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
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
        catch (Exception e) {

            // record sendNowWith logger that Map generation threw an exception
            logger.log(TreeLogger.ERROR, "Error generating extensions", e);

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

        sourceWriter.println("public void initExtensions(" + MessageBus.class.getName() + " bus) { ");
        sourceWriter.outdent();

        final List<File> targets = ConfigUtil.findAllConfigTargets();

        new SerializationExtensionGenerator().generate(context, logger, sourceWriter, targets);

        ConfigUtil.visitAllTargets(targets, context, logger, sourceWriter,
                new RebindVisitor() {
                    public void visit(Class<?> visit, GeneratorContext context, TreeLogger logger, SourceWriter writer) {
                        if (ConfigUtil.isAnnotated(visit, ExtensionComponent.class, ExtensionGenerator.class)) {
                            try {
                                ExtensionGenerator generator = visit.asSubclass(ExtensionGenerator.class).newInstance();
                                generator.generate(context, logger, writer, targets);
                            }
                            catch (Exception e) {
                                throw new RuntimeException("Could not load extension generator: " + visit.getName(), e);
                            }
                        }
                    }
                });

        // end constructor source generation
        sourceWriter.outdent();
        sourceWriter.println("}");
    }
}
