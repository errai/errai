package org.jboss.errai.bus.rebind;

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
import org.jboss.errai.bus.server.annotations.ExtensionConfigurator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.Thread.currentThread;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

public class ExtensionProxyGenerator extends Generator {
    private List<ExtensionGenerator> generators;
    private List<File> targets;

    /**
     * Simple name of class to be generated
     */
    private String className = null;

    /**
     * Package name of class to be generated
     */
    private String packageName = null;


    @Override
    public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
        findAllTargets();
        findAllGenerators();

        TypeOracle typeOracle = context.getTypeOracle();

        generators = new LinkedList<ExtensionGenerator>();

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

        composer.addImplementedInterface(ExtensionsLoader.class.getName());

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
        // start constructor source generation
        sourceWriter.println("public " + className + "() { ");
        sourceWriter.indent();
        sourceWriter.println("super();");
        sourceWriter.outdent();
        sourceWriter.println("}");

        sourceWriter.println("public void initExtensions(" + MessageBus.class.getName() + " bus) { ");
        sourceWriter.outdent();

        for (ExtensionGenerator generator : generators) {
            generator.generate(logger, sourceWriter, targets);
        }

        // end constructor source generation
        sourceWriter.outdent();
        sourceWriter.println("}");
    }

    private void findAllGenerators() {
        for (File targetRoot : targets) {
            findAndRegisterGenerators(targetRoot);
        }
    }

    private void findAndRegisterGenerators(File root) {
        _findGenerators(root, root, generators);
    }

    private static void _findGenerators(File root, File start, List<ExtensionGenerator> generators) {
        for (File file : start.listFiles()) {
            if (file.isDirectory()) _findGenerators(root, file, generators);
            if (file.getName().endsWith(".class")) {
                try {
                    String FQCN = getCandidateFQCN(root.getAbsolutePath(), file.getAbsolutePath());
                    Class<?> clazz = Class.forName(FQCN);

                    if (isAnnotated(clazz, ExtensionConfigurator.class, ExtensionGenerator.class)) {
                        ExtensionGenerator generator;
                        try {
                            generator = clazz.asSubclass(ExtensionGenerator.class).newInstance();
                            generators.add(generator);

                        }
                        catch (Exception e) {
                            throw new RuntimeException("Could not load extension generator: " + clazz.getName(), e);
                        }
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

    private static String getCandidateFQCN(String rootFile, String fileName) {
        return fileName.replaceAll("(/|\\\\)", ".")
                .substring(rootFile.length() + 1, fileName.lastIndexOf('.'));
    }

    private static boolean isAnnotated(Class clazz, Class<? extends Annotation> annotation, Class ofType) {
        if (ofType.isAssignableFrom(clazz)) {
            return clazz.isAnnotationPresent(annotation);
        } else {
            throw new RuntimeException("Unknown type annotated with: " + annotation.getName());
        }
    }

    private void findAllTargets() {

        try {
            Enumeration<URL> t = currentThread().getContextClassLoader().getResources("ErraiApp.properties");
            targets = new LinkedList<File>();
            while (t.hasMoreElements()) {
                targets.add( new File(t.nextElement().getFile()).getParentFile());
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Could not generate extension proxies", e);
        }

    }
}
