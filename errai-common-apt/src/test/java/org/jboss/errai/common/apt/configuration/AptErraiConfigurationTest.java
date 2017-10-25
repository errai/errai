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
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class AptErraiConfigurationTest extends ErraiAptTest {

  @Test
  public void testNewWithUnannotatedMetaClass() {
    try {
      new AptErraiAppConfiguration(aptClass(String.class));
      Assert.fail("An exception should've been raised because String.class is not annotated with @ErraiApp");
    } catch (final Exception e) {
      Assert.assertTrue(e instanceof RuntimeException);
    }
  }

  @Test
  public void testGetAllPropertiesWithDefaultValues() {
    final AptErraiAppConfiguration config = new AptErraiAppConfiguration(aptClass(ErraiTestAppWithDefaultValues.class));

    Assert.assertEquals("", config.getApplicationContext());
    Assert.assertEquals(false, config.isUserEnabledOnHostPage());
    Assert.assertEquals(false, config.asyncBeanManager());
    Assert.assertEquals(false, config.isWebSocketServerEnabled());
    Assert.assertEquals(false, config.isAutoDiscoverServicesEnabled());
    Assert.assertEquals(false, config.forceStaticMarshallers());
    Assert.assertEquals(false, config.useStaticMarshallers());
    Assert.assertEquals(false, config.makeDefaultArrayMarshallers());
    Assert.assertEquals(false, config.lazyLoadBuiltinMarshallers());
    Assert.assertFalse(config.custom("nonexistent").isPresent());
  }

  @Test
  public void testGetAllPropertiesWithCustomValues() {
    final AptErraiAppConfiguration config = new AptErraiAppConfiguration(aptClass(ErraiTestApp.class));

    Assert.assertEquals(true, config.isUserEnabledOnHostPage());
    Assert.assertEquals(true, config.asyncBeanManager());
    Assert.assertEquals(true, config.isWebSocketServerEnabled());
    Assert.assertEquals(true, config.isAutoDiscoverServicesEnabled());
    Assert.assertEquals(true, config.forceStaticMarshallers());
    Assert.assertEquals(true, config.useStaticMarshallers());
    Assert.assertEquals(true, config.makeDefaultArrayMarshallers());
    Assert.assertEquals(true, config.lazyLoadBuiltinMarshallers());
    Assert.assertEquals("test", config.custom("existent").get());
  }
}
