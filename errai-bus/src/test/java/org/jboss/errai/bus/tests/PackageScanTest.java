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
package org.jboss.errai.bus.tests;

import junit.framework.TestCase;
import org.jboss.errai.bus.server.annotations.ApplicationComponent;
import org.jboss.errai.bus.server.annotations.ExtensionComponent;
import org.jboss.errai.bus.server.service.metadata.MetaDataScanner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Test the VFS extensions for Reflections and verify it can read from ear files.
 *  
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Aug 5, 2010
 */
public class PackageScanTest extends TestCase
{
  public void testEarScan() throws Exception
  {    
    File ear = new File("src/test/resources/helloworld.ear");
    assertTrue(ear.exists());
    URL earUrl = new URL(ear.toURI().toString()+"/helloworld.war!/WEB-INF/classes");
    URL libUrl = new URL(ear.toURI().toString()+"/helloworld.war!/WEB-INF/lib/errai-tools-1.1-SNAPSHOT.jar");

    List<URL> urlList = new ArrayList<URL>();
    urlList.add(earUrl);
    urlList.add(libUrl);
    
    MetaDataScanner scanner = MetaDataScanner.createInstance(urlList);

    // nested in ear/war/WEB-INF/classes
    Set<String> classesMeta = scanner.getStore().getTypesAnnotatedWith(ApplicationComponent.class.getName());
    assertFalse("Cannot find @ApplicationComponent on HelloWorldService", classesMeta.isEmpty());

    // nested in ear/war/WEB-INF/lib
    Set<String> libMeta = scanner.getStore().getTypesAnnotatedWith(ExtensionComponent.class.getName());
    boolean match = false;
    for(String className : libMeta)
    {
      if("org.jboss.errai.tools.monitoring.MonitorExtension".equals(className))
      {
        match = true;
        break;
      }
    }

    assertTrue("Cannot find @ExtensionComponent on MonitorExtension", match);
  }                                  

  public void testWarScan() throws Exception
  {
    File war = new File("src/test/resources/helloworld.war");
    assertTrue(war.exists());
    URL warUrl = war.toURI().toURL();

    List<URL> urlList = new ArrayList<URL>();
    urlList.add(warUrl);
    MetaDataScanner scanner = MetaDataScanner.createInstance(urlList);

    Set<String> annotated = scanner.getStore().getTypesAnnotatedWith(ApplicationComponent.class.getName());
    assertFalse("Cannot find @ApplicationComponent on HelloWorldService", annotated.isEmpty());
  }

  // w/o we cannot create VFS Url's for this test
  /*class VFSHandler extends URLStreamHandler
  {
    @Override
    protected URLConnection openConnection(URL u) throws IOException
    {
      throw new RuntimeException("Not implemented");
    }
  }*/
}
