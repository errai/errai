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

package org.jboss.errai.ioc.tests.wiring.client;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Runs {@link org.jboss.errai.ioc.tests.wiring.client.prod.JsTypeInjectionTest}
 * in production mode.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@RunWith(Suite.class)
@SuiteClasses(org.jboss.errai.ioc.tests.wiring.client.prod.JsTypeInjectionTest.class)
public class JsTypeInjectionTest {

  private static String originalGwtArgs;

  @BeforeClass
  public static void enableProductionMode() {
    originalGwtArgs = System.getProperty("gwt.args", "");
    System.setProperty("gwt.args", originalGwtArgs + " -prod");
  }

  @AfterClass
  public static void disableProductionMode() {
    System.setProperty("gwt.args", originalGwtArgs);
  }

}
