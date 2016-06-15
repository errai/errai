/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.tests;

import static org.junit.Assert.*;

import org.jboss.errai.bus.server.service.ErraiConfigAttribs;
import org.jboss.errai.bus.server.service.ErraiServiceConfiguratorImpl;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class ErraiServiceConfiguratorTest {

  @Before
  public void setup() {
    System.setProperty("errai.service_config_prefix_path", "org.jboss.errai.bus.tests.config");
  }

  @Test
  public void systemPropertyOverridesErraiServiceProperty() throws Exception {
    final ErraiServiceConfiguratorImpl config = new ErraiServiceConfiguratorImpl();
    assertTrue(ErraiConfigAttribs.ENABLE_SSE_SUPPORT.getBoolean(config));
    System.setProperty(ErraiConfigAttribs.ENABLE_SSE_SUPPORT.getAttributeName(), "false");
    assertFalse(ErraiConfigAttribs.ENABLE_SSE_SUPPORT.getBoolean(config));
  }

}
