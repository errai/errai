package org.jboss.errai.bus.server.util;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.user.rebind.SourceWriter;

import java.io.File;
import static java.lang.Thread.currentThread;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.*;

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

    public static void visitAll(File root, final TreeLogger logger, final SourceWriter writer, final RebindVisitor visitor) {
       _findLoadableModules(root, root, new HashSet<String>(), new VisitDelegate() {
           public void visit(Class clazz) {
               visitor.visit(clazz, logger, writer);
           }
       });
    }

    public static void visitAllTargets(List<File> targets,  final TreeLogger logger, final SourceWriter writer, RebindVisitor visitor) {
        for (File file : targets) {
            visitAll(file, logger, writer, visitor);
        }
    }

    private static void _findLoadableModules(File root, File start, Set<String> loadedTargets, VisitDelegate visitor) {
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
