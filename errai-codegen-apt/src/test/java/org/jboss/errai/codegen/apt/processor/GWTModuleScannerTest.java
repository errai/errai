/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.codegen.apt.processor;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.jboss.errai.codegen.apt.processor.GWTModuleScanner.ScanResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@RunWith(JUnit4.class)
public class GWTModuleScannerTest {

  private final GWTModuleScanner scanner = new GWTModuleScanner();

  @Test
  public void loadModuleGraph() throws Exception {
    final URL parent = moduleUrl("org/jboss/errai/codegen/apt/processor/A.gwt.xml");
    final List<GWTModule> expectedScanned = asList(
            new GWTModule(moduleUrl("org/jboss/errai/codegen/apt/processor/A.gwt.xml"),
                          new URL[] {subDir(parent, "client")},
                          new String[] {"org.jboss.errai.codegen.apt.processor.B"}),
            new GWTModule(moduleUrl("org/jboss/errai/codegen/apt/processor/B.gwt.xml"),
                          new URL[] {subDir(parent, "client")},
                          new String[] {"org.jboss.errai.codegen.apt.processor.C",
                                        "org.jboss.errai.codegen.apt.processor.D"}),
            new GWTModule(moduleUrl("org/jboss/errai/codegen/apt/processor/C.gwt.xml"),
                          new URL[] {subDir(parent, "client")},
                          new String[] {"org.jboss.errai.codegen.apt.processor.D"}),
            new GWTModule(moduleUrl("org/jboss/errai/codegen/apt/processor/D.gwt.xml"),
                          new URL[] {subDir(parent, "foo"), subDir(parent, "bar")},
                          new String[] {"org.jboss.errai.codegen.apt.processor.E"})

            );
    final List<String> expectedMissing = singletonList("org.jboss.errai.codegen.apt.processor.E");
    final ScanResult result = scanner.scanFromRoot("org.jboss.errai.codegen.apt.processor.A");

    assertNotNull(result);
    assertEquals(expectedMissing, result.getMissing());
    assertEquals(expectedScanned.size(), result.getScanned().size());
    for (int i = 0; i < expectedScanned.size(); i++) {
      final GWTModule expected = expectedScanned.get(i);
      final GWTModule actual = result.getScanned().get(i);
      assertEquals("Difference in module XML URL of module " + i , expected.getModuleXml(), actual.getModuleXml());
      assertArrayEquals("Difference in inherited modules of module " + i , expected.getInheritedModuleNames(), actual.getInheritedModuleNames());
      assertArrayEquals("Difference in source paths of module " + i , expected.getSourcePaths(), actual.getSourcePaths());
    }
  }

  private URL moduleUrl(final String path) {
    return getClass().getClassLoader().getResource(path);
  }

  private URL subDir(final URL parent, final String child) throws MalformedURLException {
    final File parentFile = new File(parent.getFile()).getParentFile();
    return new File(parentFile, child).toURI().toURL();
  }

}
