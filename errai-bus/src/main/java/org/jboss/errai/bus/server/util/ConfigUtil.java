package org.jboss.errai.bus.server.util;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.user.rebind.SourceWriter;

import java.io.File;
import static java.lang.Thread.currentThread;

import java.io.FileInputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ConfigUtil {
    public static final String ERRAI_CONFIG_STUB_NAME = "ErraiApp.properties";

    public static List<File> findAllConfigTargets() {
        try {
            Enumeration<URL> t = currentThread().getContextClassLoader().getResources(ERRAI_CONFIG_STUB_NAME);
            List<File> targets = new LinkedList<File>();
            while (t.hasMoreElements()) {
                targets.add(new File(t.nextElement().getFile()).getParentFile());
            }

            return targets;
        }
        catch (Exception e) {
            throw new RuntimeException("Could not generate extension proxies", e);
        }
    }

    public static void visitAll(File root, final ConfigVisitor visitor) {
        _findLoadableModules(root, root, new HashSet<String>(), new VisitDelegate() {
            public void visit(Class clazz) {
                visitor.visit(clazz);
            }
        });
    }

    public static void visitAllTargets(List<File> targets, ConfigVisitor visitor) {
        for (File file : targets) {
            visitAll(file, visitor);
        }
    }

    public static void visitAll(File root, final GeneratorContext context, final TreeLogger logger, final SourceWriter writer, final RebindVisitor visitor) {
        _findLoadableModules(root, root, new HashSet<String>(), new VisitDelegate() {
            public void visit(Class clazz) {
                visitor.visit(clazz, context, logger, writer);
            }
        });
    }

    public static void visitAllTargets(List<File> targets, final GeneratorContext context, final TreeLogger logger, final SourceWriter writer, RebindVisitor visitor) {
        for (File file : targets) {
            visitAll(file, context, logger, writer, visitor);
        }
    }

    private static void _findLoadableModules(File root, File start, Set<String> loadedTargets, VisitDelegate visitor) {
        if(start.isDirectory())
            loadFromDirectory(root, start, loadedTargets, visitor);
        else
            loadFromJAR(root, start, loadedTargets, visitor);

    }

    private static void loadFromJAR(File root, File start, Set<String> loadedTargets, VisitDelegate visitor)
    {
        final String pathToJar = start.getPath();

        if(!pathToJar.startsWith("file:/") || !pathToJar.endsWith(".jar!"))
            throw new RuntimeException("Not a jar: "+start.getAbsolutePath());

        try{
            String jarName = pathToJar.substring(5, pathToJar.length()-1);
            JarInputStream jarFile = new JarInputStream (new FileInputStream(jarName));
            JarEntry jarEntry;

            while(true)
            {
                jarEntry=jarFile.getNextJarEntry ();
                if(jarEntry == null){
                    break;
                }
                if(jarEntry.getName ().endsWith (".class") )
                {
                    String classEntry = jarEntry.getName().replaceAll("/", "\\.");
                    String className = classEntry.substring(0, classEntry.indexOf(".class"));
                    
                    Class<?> loadClass = Class.forName(className);
                    visitor.visit(loadClass);
                }
            }
        }
        catch( Exception e)
        {
            throw new RuntimeException("Failed to scan jar", e);
        }
    }

    private static void loadFromDirectory(File root, File start, Set<String> loadedTargets, VisitDelegate visitor) {
        for (File file : start.listFiles()) {
            if (file.isDirectory()) _findLoadableModules(root, file, loadedTargets, visitor);
            if (file.getName().endsWith(".class")) {
                try {
                    String FQCN = getCandidateFQCN(root.getAbsolutePath(), file.getAbsolutePath());

                    if (loadedTargets.contains(FQCN)) {
                        return;
                    } else {
                        loadedTargets.add(FQCN);
                    }

                    Class<?> loadClass = Class.forName(FQCN);

                    visitor.visit(loadClass);
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
                catch (UnsatisfiedLinkError e) {
                    // do nothing.
                }
            }
        }
    }

    private static String getCandidateFQCN(String rootFile, String fileName) {
        return fileName.replaceAll("(/|\\\\)", ".")
                .substring(rootFile.length() + 1, fileName.lastIndexOf('.'));
    }

    public static boolean isAnnotated(Class clazz, Class<? extends Annotation> annotation, Class ofType) {
        return ofType.isAssignableFrom(clazz) && clazz.isAnnotationPresent(annotation);
    }
}
