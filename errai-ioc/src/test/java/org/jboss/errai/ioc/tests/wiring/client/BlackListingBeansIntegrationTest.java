/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCResolutionException;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.jboss.errai.ioc.tests.wiring.client.res.BlacklistedBean;
import org.jboss.errai.ioc.tests.wiring.client.res.sub.BlacklistedPackageBean;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class BlackListingBeansIntegrationTest extends AbstractErraiIOCTest {
  
  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.tests.wiring.IOCWiringTests";
  }
  
  public void testBlacklistedBean() throws Exception {
    try {
      IOC.getBeanManager().lookupBean(BlacklistedBean.class).getInstance();
      fail("Should not be able to resolve a blacklisted bean!");
    }
    catch (IOCResolutionException e) {
      // expected
    }
  }
  
  public void testBlacklistedPackage() throws Exception {
    try {
      IOC.getBeanManager().lookupBean(BlacklistedPackageBean.class).getInstance();
      fail("Should not be able to resolve a bean in a blacklisted package!");
    }
    catch (IOCResolutionException e) {
      // expected
    }
  }
}
