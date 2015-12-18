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

package org.jboss.errai.enterprise.jaxrs.client.test;

import org.jboss.errai.common.client.api.interceptor.InterceptsRemoteCall;
import org.jboss.errai.enterprise.client.jaxrs.test.AbstractErraiJaxrsTest;
import org.jboss.errai.enterprise.jaxrs.client.shared.InterceptsRemoteCallTestService;
import org.junit.Test;

/**
 * Tests to ensure that the {@link InterceptsRemoteCall} annotation works properly.
 */
public class InterceptsRemoteCallIntegrationTest extends AbstractErraiJaxrsTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.enterprise.jaxrs.TestModule";
  }

  @Test
  public void testInterceptedRestCall1() {
    call(InterceptsRemoteCallTestService.class,
        new AssertionCallback<String>("Request was not intercepted", "intercepted"))
        .interceptedGet1();
  }
  
  @Test
  public void testInterceptedRestCall2() {
    call(InterceptsRemoteCallTestService.class,
        new AssertionCallback<String>("Request was not intercepted", "intercepted"))
        .interceptedGet2();
  }
}
