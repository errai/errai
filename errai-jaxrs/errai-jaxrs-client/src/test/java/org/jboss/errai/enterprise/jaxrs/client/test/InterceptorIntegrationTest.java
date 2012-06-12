/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.enterprise.jaxrs.client.test;

import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.enterprise.client.jaxrs.test.AbstractErraiJaxrsTest;
import org.jboss.errai.enterprise.jaxrs.client.shared.InterceptedTestService;
import org.junit.Test;

/**
 * Tests to ensure Errai JAX-RS can marshal/demarshal Jackson generated JSON.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class InterceptorIntegrationTest extends AbstractErraiJaxrsTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.enterprise.jaxrs.TestModule";
  }

  @Test
  public void testInterceptedRestCallBypassingRemoteEndpoint() {
    delayTestFinish(5000);

    RestClient.create(InterceptedTestService.class,
        new RemoteCallback<String>() {
          @Override
          public void callback(String result) {
            assertEquals("Request was not intercepted", "intercepted", result);
            finishTest();
          }
        }).interceptedGetBypassingEndpoint();
  }

  @Test
  public void testInterceptedRestCallWithResultManipulation() {
    delayTestFinish(5000);
    RestClient.create(InterceptedTestService.class,
        new RemoteCallback<String>() {
          @Override
          public void callback(String result) {
            assertEquals("Request was not intercepted", "result_intercepted", result);
            finishTest();
          }
        }).interceptedGetManipulatingResult("will be removed by interceptor");
  }

}