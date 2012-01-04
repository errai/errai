/*
 * Copyright 2011 JBoss, a division of Red Hat, Inc
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

package org.jboss.errai.common.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.junit.Test;
import org.reflections.vfs.ZipDir;

/**
 * Tests for {@link MetaDataScanner}.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class MetaDataScannerTest {

  @Test
  public void testUrlDecodingWithSimpleName() throws Exception {
    final String jarFileName = "test.jar";

    URL testJarURL = getClass().getClassLoader().getResource(jarFileName);
    assertNotNull("Jar file not found:" + testJarURL, testJarURL);
    
    ClassLoader loader = URLClassLoader.newInstance(new URL[] { testJarURL }, getClass().getClassLoader());

    List<URL> urls = MetaDataScanner.getConfigUrls(loader);
    
    String[] segments = urls.get(0).getPath().split("/");
    assertTrue("No path segments found", segments.length > 0);
    assertEquals("URL not properly decoded", jarFileName +"!", segments[segments.length - 1]);
    assertNotNull("Could not open jar", new ZipDir(urls.get(0)).getFiles());
  }
  
  @Test
  public void testUrlDecodingWithSpecialCharacters() throws Exception {
    final String jarFileName = "testjar-\u00e4\u00f6\u00e9\u00f8 +abc!@$%^&*()_+}{.jar";

    URL testJarURL = getClass().getClassLoader().getResource(jarFileName);
    assertNotNull("Jar file not found: " + jarFileName, testJarURL);

    ClassLoader loader = URLClassLoader.newInstance(new URL[] { testJarURL }, getClass().getClassLoader());

    List<URL> urls = MetaDataScanner.getConfigUrls(loader);
    
    String[] segments = urls.get(0).getPath().split("/");
    assertTrue("No path segments found", segments.length > 0);
    assertEquals("URL not properly decoded", jarFileName +"!", segments[segments.length - 1]);
    assertNotNull("Could not open jar", new ZipDir(urls.get(0)).getFiles());
  }
}
