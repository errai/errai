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

package org.jboss.errai.bus.server.util;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.user.rebind.SourceWriter;

import java.io.*;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ConfigUtil {
    public static final String ERRAI_CONFIG_STUB_NAME = "ErraiApp.properties";

    public static List<File> findAllConfigTargets() {
        try {
            Enumeration<URL> t = ConfigUtil.class.getClassLoader().getResources(ERRAI_CONFIG_STUB_NAME);
            List<File> targets = new LinkedList<File>();
            while (t.hasMoreElements()) {
                String fileName = t.nextElement().getFile();
                int trimIdx = fileName.lastIndexOf("!");
                if (trimIdx != -1) {
                    fileName = fileName.substring(0, trimIdx);
                }

                if (fileName.startsWith("file:/")) {
                    fileName = fileName.substring(5);
                }
                
                if (fileName.endsWith("-sources.jar")) {
                    continue;
                }
                File file = new File(fileName);

                targets.add(trimIdx == -1 ? file.getParentFile() : file);
            }

            System.out.println("Scan Targets:");
            for (File tg : targets) {
                System.out.println(" -> " + tg.getPath());
            }

            return targets;
        }
        catch (Exception e) {
            throw new RuntimeException("Could not generate extension proxies", e);
        }
    }

    private static Map<String, File> scanAreas = new HashMap<String, File>();
    private static Map<String, List<Class>> scanCache = new HashMap<String, List<Class>>();
    private static Set<String> activeCacheContexts = new HashSet<String>();

    private static String tmpUUID = "erraiBootstrap_" + UUID.randomUUID().toString().replaceAll("\\-", "_");

    private static void recordCache(String context, Class cls) {
        List<Class> cache = scanCache.get(context);

        if (cache == null) {
            System.out.println("adding context '" + context + "'");
            scanCache.put(context, cache = new LinkedList<Class>());
        }

        cache.add(cls);
    }

    public static void cleanupStartupTempFiles() {        
        for (File f : scanAreas.values()) {
            f.delete();
        }
        new File(System.getProperty("java.io.tmpdir") + "/" + tmpUUID).delete();
        scanAreas = null;
        scanCache = null;
    }

    public static void visitAll(File root, final ConfigVisitor visitor) {
        _findLoadableModules(root, root, new HashSet<String>(), new VisitDelegate() {
            public void visit(Class clazz) {
                visitor.visit(clazz);
            }
        });
        activeCacheContexts.add(root.getPath());
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
        activeCacheContexts.add(root.getPath());
    }

    public static void visitAllTargets(List<File> targets, final GeneratorContext context, final TreeLogger logger, final SourceWriter writer, RebindVisitor visitor) {
        for (File file : targets) {
            visitAll(file, context, logger, writer, visitor);
        }
    }

    private static void _findLoadableModules(File root, File start, Set<String> loadedTargets, VisitDelegate visitor) {
        if (start.isDirectory()) {
            loadFromDirectory(root, start, loadedTargets, visitor);
        } else if (start.isFile()) {
            loadFromZippedResource(root, start, loadedTargets, visitor, null);
        } else {
            /**
             * This path is not directly resolvable to a directory or file target, which may mean that it's a
             * virtual path.  So in order to over-come this, we'll perform a brute-force reparsing of the URI
             * until we find something to work with.
             */
            findValidPath(root, start, loadedTargets, visitor);
        }
    }

    private static void findValidPath(File root, File start, Set<String> loadedTargets, VisitDelegate visitor) {
        String originalPath = start.getPath();
        int pivotPoint;

        String rootPath;
        do {
            start = new File((rootPath = start.getPath())
                    .substring(0, (pivotPoint = rootPath.lastIndexOf("/")) < 0 ? 0 : pivotPoint));

        } while (!start.isFile() && pivotPoint > 0);

        if (start.isFile()) {
            loadFromZippedResource(root, start, loadedTargets, visitor, originalPath.substring(pivotPoint + 1));
        }
    }

    private static String CLASS_RESOURCES_ROOT = "WEB-INF.classes.";

    private static void loadFromZippedResource(File root, File start, Set<String> loadedTargets, VisitDelegate visitor, String scanFilter) {
        final String pathToJar = start.getPath();
        boolean startsWithFile = pathToJar.startsWith("file:/");

        InputStream inStream = null;
        try {
            if (!pathToJar.matches(".+\\.(zip|jar|war)$")) return;

            int startIdx = startsWithFile ? 5 : 0;
            int endIdx = pathToJar.lastIndexOf(".jar");
            if (endIdx == -1) endIdx = pathToJar.lastIndexOf(".war");
            if (endIdx == -1) endIdx = pathToJar.lastIndexOf(".zip");

            if (endIdx == -1) {
                endIdx = pathToJar.length() - 1;
            } else {
                endIdx += 4;
            }

            String jarName = pathToJar.substring(startIdx, endIdx);

            inStream = findResource(ConfigUtil.class.getClassLoader(), jarName.replaceAll("/", "\\."));

            if (inStream == null) {
                /**
                 * Try to load this directly as a file.
                 */
                inStream = new FileInputStream(jarName);
            }
            loadZipFromStream(jarName, inStream, loadedTargets, visitor, scanFilter);

        }
        catch (Exception e) {
            System.out.println("Skipped:" + pathToJar + " (" + e.getMessage() + ")");
            e.printStackTrace();
        }
        finally {
            try {
                if (inStream != null) inStream.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void loadZipFromStream(String zipName, InputStream inStream, Set<String> loadedTargets,
                                          VisitDelegate visitor, String scanFilter) throws IOException {
        ZipInputStream zipFile = new ZipInputStream(inStream);
        ZipEntry zipEntry;

        String ctx = zipName + (scanFilter == null ? ":*" : ":" + scanFilter);

        if (activeCacheContexts.contains(ctx) && scanCache.containsKey(ctx)) {
            List<Class> cache = scanCache.get(ctx);
            for (Class loadClass : cache) {
                visitor.visit(loadClass);
            }
        } else {
            while ((zipEntry = zipFile.getNextEntry()) != null) {
                if (scanFilter != null && !zipEntry.getName().startsWith(scanFilter)) continue;

                if (zipEntry.getName().endsWith(".class")) {
                    //   System.out.println("ScanningEntry: " + zipEntry.getName());
                    String classEntry;
                    String className = null;
                    boolean cached = false;
                    try {
                        classEntry = zipEntry.getName().replaceAll("/", "\\.");
                        int beginIdx = classEntry.indexOf(CLASS_RESOURCES_ROOT);
                        if (beginIdx == -1) {
                            beginIdx = 0;
                        } else {
                            beginIdx += CLASS_RESOURCES_ROOT.length();
                        }

                        className = classEntry.substring(beginIdx, classEntry.lastIndexOf(".class"));
                        Class<?> loadClass = Class.forName(className);
                        recordCache(ctx, loadClass);

                        cached = true;

                        visitor.visit(loadClass);
                    }
                    catch (Throwable e) {
                        if (!cached) {
                            System.out.println("Could not load: " + className + " (" + e.getMessage() + ")");
                        }
                    }
                } else if (zipEntry.getName().matches(".+\\.(zip|jar|war)$")) {
                    /**
                     * Let's decompress this to a temp dir so we can look at it:
                     */

                    InputStream tmpZipStream = null;
                    try {

                        if (scanAreas.containsKey(zipEntry.getName())) {
                            File tmpFile = scanAreas.get(zipEntry.getName());
                            tmpZipStream = new FileInputStream(tmpFile);
                            loadZipFromStream(tmpFile.getName(), tmpZipStream, loadedTargets, visitor, null);
                        } else {
                            File tmpUnZip = expandZipEntry(zipFile, zipEntry);

                            scanAreas.put(zipEntry.getName(), tmpUnZip);

                            tmpZipStream = new FileInputStream(tmpUnZip);

                            loadZipFromStream(tmpUnZip.getName(), tmpZipStream, loadedTargets, visitor, null);
                        }
                    }
                    finally {
                        if (tmpZipStream != null) {
                            tmpZipStream.close();
                        }
                    }
                }
            }

            activeCacheContexts.add(zipName);
        }
    }

    private static File expandZipEntry(ZipInputStream stream, ZipEntry entry) {
        String tmpDir = System.getProperty("java.io.tmpdir") + "/" + tmpUUID;
        int idx = entry.getName().lastIndexOf('/');
        String tmpFileName = tmpDir + "/" + entry.getName().substring(idx == -1 ? 0 : idx);
        try {
            File tmpDirFile = new File(tmpDir);
            tmpDirFile.mkdirs();

            File newFile = new File(tmpFileName);

            FileOutputStream outStream = new FileOutputStream(newFile);
            byte[] buf = new byte[1024];
            int read;
            while ((read = stream.read(buf)) != -1) {
                outStream.write(buf, 0, read);
            }

            outStream.flush();
            outStream.close();

            newFile.getParentFile().deleteOnExit();

            return newFile;
        }
        catch (Exception e) {
            System.out.println("could not expand file");
            e.printStackTrace();
            return null;
        }
    }


    private static void loadFromDirectory(File root, File start, Set<String> loadedTargets, VisitDelegate visitor) {
        if (activeCacheContexts.contains(root.getPath()) && scanCache.containsKey(root.getPath())) {
            for (Class loadClass : scanCache.get(root.getPath())) {
                visitor.visit(loadClass);
            }
        } else {
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

                        recordCache(root.getPath(), loadClass);

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
    }

    private static InputStream findResource(ClassLoader loader, String resourceName) {
        ClassLoader cl = loader;
        InputStream is = null;

        while ((is = cl.getResourceAsStream(resourceName)) == null && (cl = cl.getParent()) != null) ;

        return is;
    }

    private static String getCandidateFQCN(String rootFile, String fileName) {
        return fileName.replaceAll("(/|\\\\)", ".")
                .substring(rootFile.length() + 1, fileName.lastIndexOf('.'));
    }

    public static boolean isAnnotated(Class clazz, Class<? extends Annotation> annotation, Class ofType) {
        return ofType.isAssignableFrom(clazz) && clazz.isAnnotationPresent(annotation);
    }
}
