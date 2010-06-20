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
import org.jboss.errai.bus.server.ErraiBootstrapFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Contains methods used for configuring and bootstrapping Errai.
 */
public class ConfigUtil {
    public static final String ERRAI_CONFIG_STUB_NAME = "ErraiApp.properties";
    public static final Logger log = LoggerFactory.getLogger(ConfigUtil.class);

    /**
     * Gets a list of all the configuration targets in the form of <tt>File</tt>s
     *
     * @return a <tt>File</tt> list of all the configuration targets
     */
    public static List<File> findAllConfigTargets() {
        try {
            Enumeration<URL> t = ConfigUtil.class.getClassLoader().getResources(ERRAI_CONFIG_STUB_NAME);
            List<File> targets = new LinkedList<File>();
            while (t.hasMoreElements()) {
                String fileName = URLDecoder.decode(t.nextElement().getFile(), "UTF-8");

                // this is referencing a file inside a compressed archive.
                int trimIdx = fileName.lastIndexOf("!");
                if (trimIdx != -1) {
                    // get the path to the archive
                    fileName = fileName.substring(0, trimIdx);
                }

                // if it starts with a URI scheme prefix, let's strip it away.
                if (fileName.startsWith("file:/")) {
                    fileName = fileName.substring(5);
                }

                // we don't want to bother with source JARs.
                if (fileName.endsWith("-sources.jar")) {
                    continue;
                }

                // obtain a File object
                File file = new File(fileName);

                // If this is a direct filesystem path, we get the parent file (directory)
                targets.add(trimIdx == -1 ? file.getParentFile() : file);
            }

            log.info("configuration scan targets");
            for (File tg : targets) {
                log.info(" -> " + tg.getPath());
            }

            return targets;
        }
        catch (Exception e) {
            throw new ErraiBootstrapFailure("could not locate config target paths", e);
        }
    }

    private static Map<String, File> scanAreas = new HashMap<String, File>();
    private static Map<String, List<Class>> scanCache = new HashMap<String, List<Class>>();
    private static Set<String> cacheBlackList = new HashSet<String>();
    private static Set<String> activeCacheContexts = new HashSet<String>();

    private static String tmpUUID = "erraiBootstrap_" + UUID.randomUUID().toString().replaceAll("\\-", "_");

    private static void recordCache(String context, Class cls) {
        if (scanCache == null || cacheBlackList.contains(context)) return;

        List<Class> cache = scanCache.get(context);

        if (cache == null) {
            log.info("caching context '" + context + "'");
            scanCache.put(context, cache = new LinkedList<Class>());
        }

        cache.add(cls);
    }

    /**
     * Cleans up the startup temporary files, including those stored under the system's temp directory
     */
    public static void cleanupStartupTempFiles() {
        if (scanAreas == null) return;

        log.info("Cleaning up ...");
        for (File f : scanAreas.values()) {
            f.delete();
        }
        new File(System.getProperty("java.io.tmpdir") + "/" + tmpUUID).delete();
        scanAreas = null;
        scanCache = null;
    }

    public static void visitAllErraiAppProperties(final List<File> targets, final BundleVisitor visitor) {
        for (File file : targets) {
            File propertyFile = new File(file.getPath() + "/" + "ErraiApp.properties");

            if (propertyFile.exists()) {
                try {
                    FileInputStream stream = new FileInputStream(propertyFile);
                    try {
                        visitor.visit(new PropertyResourceBundle(stream));
                    }
                    finally {
                        stream.close();
                    }
                }
                catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    /**
     * Visits all targets that can be found under <tt>root</tt>, using the <tt>ConfigVisitor</tt> specified
     *
     * @param root    - the root file to start visiting from
     * @param visitor - the visitor delegate to use
     */
    public static void visitAll(File root, final ConfigVisitor visitor) {
        _traverseFiles(root, root, new HashSet<String>(), new VisitDelegate<Class>() {
            public void visit(Class clazz) {
                visitor.visit(clazz);
            }

            public void visitError(String className, Throwable t) {
            }

            public String getFileExtension() {
                return ".class";
            }
        });

        if (activeCacheContexts != null) activeCacheContexts.add(root.getPath());
    }

    /**
     * Visits all the targets listed in the file, using the <tt>ConfigVisitor</tt> specified
     *
     * @param targets - the file targets to visit
     * @param visitor - the visitor delegate to use
     */
    public static void visitAllTargets(List<File> targets, ConfigVisitor visitor) {
        for (File file : targets) {
            visitAll(file, visitor);
        }
    }

    /**
     * Visits all targets that can be found under <tt>root</tt>
     *
     * @param root    - the root file to start visiting from
     * @param context - provides metadata to deferred binding generators
     * @param logger  - log messages in deferred binding generators
     * @param writer  - supports the source file regeneration
     * @param visitor - the visitor delegate to use
     */
    public static void visitAll(File root, final GeneratorContext context, final TreeLogger logger,
                                final SourceWriter writer, final RebindVisitor visitor) {
        _traverseFiles(root, root, new HashSet<String>(), new VisitDelegate<Class>() {
            public void visit(Class clazz) {
                visitor.visit(clazz, context, logger, writer);
            }

            public void visitError(String className, Throwable t) {
                visitor.visitError(className, t);
            }

            public String getFileExtension() {
                return ".class";
            }
        });

        if (activeCacheContexts != null) activeCacheContexts.add(root.getPath());
    }

    /**
     * Visits all the file targets specified in the list using the <tt>RebindVisitor</tt>
     *
     * @param targets - the file targets to visit
     * @param context - provides metadata to deferred binding generators
     * @param logger  - log messages in deferred binding generators
     * @param writer  - supports the source file regeneration
     * @param visitor - the visitor delegate to use
     */
    public static void visitAllTargets(List<File> targets, final GeneratorContext context,
                                       final TreeLogger logger, final SourceWriter writer, RebindVisitor visitor) {
        for (File file : targets) {
            visitAll(file, context, logger, writer, visitor);
        }
    }

    private static void _traverseFiles(File root, File start, Set<String> loadedTargets, VisitDelegate visitor) {
        if (start.getPath().endsWith(".svn")) return;
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

    private static void loadFromZippedResource(File root, File start, Set<String> loadedTargets, VisitDelegate visitor,
                                               String scanFilter) {

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
            log.warn("did not process '" + pathToJar + "' (probably non-fatal)", e);
        }
        finally {
            try {
                if (inStream != null) inStream.close();
            }
            catch (Exception e) {
                log.error("failed to close stream", e);
            }
        }
    }

    private static void loadZipFromStream(String zipName, InputStream inStream, Set<String> loadedTargets,
                                          VisitDelegate visitor, String scanFilter) throws IOException {
        ZipInputStream zipFile = new ZipInputStream(inStream);
        ZipEntry zipEntry;

        String ctx = zipName + (scanFilter == null ? ":*" : ":" + scanFilter);

        boolean scanClass = ".class".equals(visitor.getFileExtension());

        if (activeCacheContexts != null && scanCache != null &&
                activeCacheContexts.contains(ctx) && scanCache.containsKey(ctx)) {
            List<Class> cache = scanCache.get(ctx);
            for (Class loadClass : cache) {
                visitor.visit(loadClass);
            }
        } else {
            while ((zipEntry = zipFile.getNextEntry()) != null) {
                if (scanFilter != null && !zipEntry.getName().startsWith(scanFilter)) continue;

                if (scanClass && zipEntry.getName().endsWith(".class")) {

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
                            log.trace("Failed to load: " + className
                                    + "(" + e.getMessage() + ") -- Probably non-fatal.");
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
            log.error("error reading from stream", e);
            return null;
        }
    }


    private static void loadFromDirectory(File root, File start, Set<String> loadedTargets, VisitDelegate visitor) {
        if (scanCache != null && activeCacheContexts != null && activeCacheContexts.contains(root.getPath())
                && scanCache.containsKey(root.getPath())) {
            for (Class loadClass : scanCache.get(root.getPath())) {
                visitor.visit(loadClass);
            }
        } else {
            for (File file : start.listFiles()) {
                if (file.isDirectory()) _traverseFiles(root, file, loadedTargets, visitor);
                if (file.getName().endsWith(".class")) {
                    String FQCN = getCandidateFQCN(root.getAbsolutePath(), file.getAbsolutePath());
                    try {
                        if (loadedTargets.contains(FQCN)) {
                            return;
                        } else {
                            loadedTargets.add(FQCN);
                        }

                        Class<?> loadClass = Class.forName(FQCN);

                        recordCache(root.getPath(), loadClass);

                        visitor.visit(loadClass);
                    }

                    catch (Throwable t) {
                    //    try {
                            cacheBlackList.add(root.getPath());
                            if (scanCache != null) scanCache.remove(root.getPath());
                            visitor.visitError(FQCN, t);
//                        }
//                        catch (Throwable t2) {
//                            t2.printStackTrace();
//                        }
                    }
                }
            }
        }
    }

    private static InputStream findResource(ClassLoader loader, String resourceName) {
        ClassLoader cl = loader;
        InputStream is;

        while ((is = cl.getResourceAsStream(resourceName)) == null && (cl = cl.getParent()) != null) ;

        return is;
    }

    private static String getCandidateFQCN(String rootFile, String fileName) {
        return fileName.replaceAll("(/|\\\\)", ".")
                .substring(rootFile.length() + 1, fileName.lastIndexOf('.'));
    }

    /**
     * Returns true if the specified class, <tt>clazz</tt>, has annotations from the class <tt>annotation</tt>. Also,
     * checks that <tt>clazz</tt> is represented by <tt>ofType</tt>
     *
     * @param clazz      - the class to check for the annotations
     * @param annotation - the annotations to look for
     * @param ofType     - the class type we want to be sure to check, as <tt>clazz</tt> could be a visitor delegate.
     * @return true if the <tt>clazz</tt> has those <tt>annotation</tt>s
     */
    public static boolean isAnnotated(Class clazz, Class<? extends Annotation> annotation, Class ofType) {
        return ofType.isAssignableFrom(clazz) && clazz.isAnnotationPresent(annotation);
    }
}
