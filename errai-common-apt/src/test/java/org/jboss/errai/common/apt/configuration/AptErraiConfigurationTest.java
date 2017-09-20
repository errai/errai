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

package org.jboss.errai.common.apt.configuration;

import org.jboss.errai.codegen.apt.test.ErraiAptTest;
import org.jboss.errai.common.apt.TestMetaClassFinder;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class AptErraiConfigurationTest extends ErraiAptTest {

  @Test
  public void testGetAllPropertiesWithDefaultValues() {
    final TestMetaClassFinder metaClassFinder = new TestMetaClassFinder(aptClass(ErraiTestAppWithDefaultValues.class));
    final AptErraiAppConfiguration config = new AptErraiAppConfiguration(metaClassFinder);

    Assert.assertEquals("", config.getApplicationContext());
    Assert.assertEquals(false, config.isUserEnabledOnHostPage());
    Assert.assertEquals(false, config.asyncBeanManager());
    Assert.assertEquals(false, config.isWebSocketServerEnabled());
    Assert.assertEquals(false, config.isAutoDiscoverServicesEnabled());
  }

  @Test
  public void testMoreThanOneErraiAppAnnotatedClasses() {
    final TestMetaClassFinder metaClassFinder = new TestMetaClassFinder(aptClass(ErraiTestApp.class),
            aptClass(ErraiTestAppWithDefaultValues.class));

    try {
      new AptErraiAppConfiguration(metaClassFinder);
    } catch (final RuntimeException e) {
      return;
    }

    Assert.fail();
  }

  @Test
  public void testGetAllPropertiesWithCustomValues() {
    final TestMetaClassFinder metaClassFinder = new TestMetaClassFinder(aptClass(ErraiTestApp.class));
    final AptErraiAppConfiguration config = new AptErraiAppConfiguration(metaClassFinder);

    Assert.assertEquals(true, config.isUserEnabledOnHostPage());
    Assert.assertEquals(true, config.asyncBeanManager());
    Assert.assertEquals(true, config.isWebSocketServerEnabled());
    Assert.assertEquals(true, config.isAutoDiscoverServicesEnabled());
  }

}
