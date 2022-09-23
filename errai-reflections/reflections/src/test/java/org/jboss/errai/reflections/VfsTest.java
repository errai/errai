/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.reflections;

import com.google.common.base.Predicates;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.jboss.errai.reflections.util.ClasspathHelper;
import org.jboss.errai.reflections.vfs.Vfs;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

/** */
@Ignore ("This tests fails on JAVA 11 due getting classloader empty where Java 8 takes the original Classpath")
public class VfsTest {

    private void testVfsDir(URL url) {
        Assert.assertNotNull(url);

        Vfs.Dir dir = Vfs.fromURL(url);
        Assert.assertNotNull(dir);

        Iterable<Vfs.File> files = dir.getFiles();
        Vfs.File first = files.iterator().next();
        Assert.assertNotNull(first);

        first.getName();
        try {
            first.openInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        dir.close();
    }

    @Test
    public void vfsFromJar() {
        testVfsDir(getSomeJar());
    }

    @Test
    public void vfsFromDir() {
        testVfsDir(getSomeDirectory());
    }

    @Test
    public void vfsFromJarFileUrl() throws MalformedURLException {
        testVfsDir(new URL("jar:file:" + getSomeJar().getPath() + "!/"));
    }

    @Test
    public void findFilesFromEmptyMatch() throws MalformedURLException {
        final URL jar = getSomeJar();
        final Iterable<Vfs.File> files = Vfs.findFiles(java.util.Arrays.asList(jar), Predicates.<Vfs.File>alwaysFalse());
        Assert.assertNotNull(files);
        Assert.assertFalse(files.iterator().hasNext());
    }

    //
    public URL getSomeJar() {
        Collection<URL> urls = ClasspathHelper.forClassLoader();
        //TODO remove this 
        System.out.println("ClasspathHelper.forClassLoader() returns "+ (urls.isEmpty()? "EMPTY": urls.toString()));
        for (URL url : urls) {
            if (url.getFile().endsWith(".jar")) {
                return url;
            }
        }

        Assert.fail("could not find jar url");
        return null;
    }

    private URL getSomeDirectory() {
        try {
            return new File(ReflectionsTest.getUserDir()).toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
