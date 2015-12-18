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

package org.jboss.errai.cdi.async;

import org.jboss.errai.cdi.async.test.bm.client.AsyncCDIBeanManagerTest;
import org.jboss.errai.cdi.async.test.cyclic.client.AsyncCyclicDepsIntegrationTest;
import org.jboss.errai.cdi.async.test.databinding.client.AsyncCDIBeanManagerDatabindingTest;
import org.jboss.errai.cdi.async.test.postconstruct.client.AsyncPostConstructOrderTest;
import org.jboss.errai.cdi.async.test.producers.client.AsyncProducerTest;
import org.jboss.errai.cdi.stereotypes.client.test.StereotypesIntegrationTest;
import org.jboss.errai.common.rebind.CacheUtil;
import org.jboss.errai.config.rebind.EnvUtil.EnvironmentConfigCache;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
@RunWith(Suite.class)
@SuiteClasses({
  AsyncCDIBeanManagerTest.class,
  AsyncCyclicDepsIntegrationTest.class,
  AsyncPostConstructOrderTest.class,
  AsyncProducerTest.class,
  AsyncCDIBeanManagerDatabindingTest.class,
  StereotypesIntegrationTest.class
})
public class AsyncCDITests {

  @BeforeClass
  public static void setup() {
    CacheUtil.getCache(EnvironmentConfigCache.class).addPermanentFrameworkProperty("errai.ioc.async_bean_manager", "true");
  }

  @AfterClass
  public static void tearDown() {
    CacheUtil.getCache(EnvironmentConfigCache.class).addPermanentFrameworkProperty("errai.ioc.async_bean_manager", "false");
  }

}
