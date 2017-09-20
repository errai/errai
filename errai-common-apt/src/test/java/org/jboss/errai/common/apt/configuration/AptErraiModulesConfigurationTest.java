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
import org.jboss.errai.common.apt.configuration.ErraiTestCustomModule1.IocWhitelisted1;
import org.jboss.errai.common.apt.configuration.ErraiTestCustomModule2.IocAlternative2;
import org.jboss.errai.common.apt.configuration.ErraiTestCustomModule2.Serializable2;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Set;

import static org.jboss.errai.common.apt.configuration.ErraiTestCustomModule1.Bindable1;
import static org.jboss.errai.common.apt.configuration.ErraiTestCustomModule1.IocAlternative1;
import static org.jboss.errai.common.apt.configuration.ErraiTestCustomModule1.IocBlacklisted1;
import static org.jboss.errai.common.apt.configuration.ErraiTestCustomModule1.NonSerializable1;
import static org.jboss.errai.common.apt.configuration.ErraiTestCustomModule1.Serializable1;
import static org.jboss.errai.common.apt.configuration.ErraiTestCustomModule2.Bindable2;
import static org.jboss.errai.common.apt.configuration.ErraiTestCustomModule2.IocBlacklisted2;
import static org.jboss.errai.common.apt.configuration.ErraiTestCustomModule2.IocWhitelisted2;
import static org.jboss.errai.common.apt.configuration.ErraiTestCustomModule2.NonSerializable2;
import static org.junit.Assert.assertTrue;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class AptErraiModulesConfigurationTest extends ErraiAptTest {

  @Test
  public void testGetAllPropertiesWithDefaultValues() {
    final TestMetaClassFinder metaClassFinder = new TestMetaClassFinder(aptClass(ErraiDefaultTestModule.class));
    final AptErraiModulesConfiguration config = new AptErraiModulesConfiguration(metaClassFinder);

    assertTrue(config.getBindableTypes().isEmpty());
    assertTrue(config.getIocEnabledAlternatives().isEmpty());
    assertTrue(config.getIocBlacklist().isEmpty());
    assertTrue(config.getIocWhitelist().isEmpty());
    assertTrue(config.getSerializableTypes().isEmpty());
    assertTrue(config.getNonSerializableTypes().isEmpty());
    assertTrue(config.getNonSerializableTypes().isEmpty());
    assertTrue(config.getNonSerializableTypes().isEmpty());
  }

  @Test
  public void testGetAllPropertiesWithCustomValues() {
    final TestMetaClassFinder metaClassFinder = new TestMetaClassFinder(aptClass(ErraiTestCustomModule1.class));
    final AptErraiModulesConfiguration config = new AptErraiModulesConfiguration(metaClassFinder);

    assertContainsOnly(config.getBindableTypes(), aptClass(Bindable1.class));
    assertContainsOnly(config.getIocEnabledAlternatives(), aptClass(IocAlternative1.class));
    assertContainsOnly(config.getIocBlacklist(), aptClass(IocBlacklisted1.class));
    assertContainsOnly(config.getIocWhitelist(), aptClass(IocWhitelisted1.class));
    assertContainsOnly(config.getSerializableTypes(), aptClass(Serializable1.class));
    assertContainsOnly(config.getNonSerializableTypes(), aptClass(NonSerializable1.class));
  }

  @Test
  public void testGetAllPropertiesComposedCustomValues() {

    final TestMetaClassFinder metaClassFinder = new TestMetaClassFinder(aptClass(ErraiTestCustomModule1.class),
            aptClass(ErraiTestCustomModule2.class));

    final AptErraiModulesConfiguration config = new AptErraiModulesConfiguration(metaClassFinder);

    assertContainsOnly(config.getBindableTypes(), aptClass(Bindable1.class), aptClass(Bindable2.class));
    assertContainsOnly(config.getIocEnabledAlternatives(), aptClass(IocAlternative1.class), aptClass(IocAlternative2.class));
    assertContainsOnly(config.getIocBlacklist(), aptClass(IocBlacklisted1.class), aptClass(IocBlacklisted2.class));
    assertContainsOnly(config.getIocWhitelist(), aptClass(IocWhitelisted1.class), aptClass(IocWhitelisted2.class));
    assertContainsOnly(config.getSerializableTypes(), aptClass(Serializable1.class), aptClass(Serializable2.class));
    assertContainsOnly(config.getNonSerializableTypes(), aptClass(NonSerializable1.class),
            aptClass(NonSerializable2.class));
  }

  private static void assertContainsOnly(final Set<?> configValue, final Object... objects) {
    Assert.assertEquals(objects.length, configValue.size());
    Assert.assertTrue(configValue.containsAll(Arrays.asList(objects)));
  }

}
