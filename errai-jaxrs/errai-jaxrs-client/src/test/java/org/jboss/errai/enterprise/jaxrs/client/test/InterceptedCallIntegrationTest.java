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

import java.util.Arrays;
import java.util.List;

import org.jboss.errai.enterprise.client.jaxrs.test.AbstractErraiJaxrsTest;
import org.jboss.errai.enterprise.jaxrs.client.shared.InterceptedCallTestService;
import org.junit.Test;

/**
 * Tests to ensure Errai JAX-RS can marshal/demarshal Jackson generated JSON.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class InterceptedCallIntegrationTest extends AbstractErraiJaxrsTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.enterprise.jaxrs.TestModule";
  }

  @Test
  public void testInterceptedRestCallWithEndpointBypassing() {
    call(InterceptedCallTestService.class,
        new AssertionCallback<String>("Request was not intercepted", "intercepted"))
        .interceptedGetWithEndpointBypassing();
  }
  
  @Test
  public void testInterceptedRestCallWithParameterManipulation() {
    call(InterceptedCallTestService.class,
        new AssertionCallback<String>("Request was not intercepted", "intercepted"))
        .interceptedGetWithParameterManipulation("will be replaced by interceptor");
  }
  
  @Test
  public void testInterceptedRestCallWithListParameterManipulation() {
    List<String> list = Arrays.asList("1", "2", "3");
    call(InterceptedCallTestService.class,
        new AssertionCallback<List<String>>("Request was not intercepted", Arrays.asList("intercepted", "2", "3")))
        .interceptedGetWithListParameterManipulation(list);
  }

  @Test
  public void testInterceptedRestCallWithResultManipulation() {
    call(InterceptedCallTestService.class,
        new AssertionCallback<String>("Request was not intercepted", "result_intercepted"))
        .interceptedGetWithResultManipulation("will be replaced by interceptor");
  }
  
  @Test
  public void testInterceptedRestCallWithChainedInterceptors() {
    call(InterceptedCallTestService.class,
        new AssertionCallback<String>("Request was not intercepted", "ABCD"))
        .interceptedGetWithChainedInterceptors("");
  }
  
  @Test
  public void testInterceptedRestCallWithPrimitiveAndBoxedParameters() {
    call(InterceptedCallTestService.class,
        new AssertionCallback<String>("Request was not intercepted", "intercepted"))
        .interceptedGetWithPrimitiveAndBoxedParameters(1l, 2l);
  }
  
  @Test
  public void testInterceptedRestCallWithResponseCallback() {
    call(InterceptedCallTestService.class,
        new AssertionCallback<String>("Request was not intercepted", "result_intercepted"))
        .interceptedGetWithResponseCallback("result");
  }
  
  @Test
  public void testInterceptedRestCallWithResponseAndErrorCallback() {
    call(InterceptedCallTestService.class,
        new AssertionCallback<String>("Request was not intercepted", "result_intercepted"))
        .interceptedGetWithResponseAndErrorCallback("result");
  }
  
  @Test
  public void testInterceptedRestCallWithClientErrorCanSucceed() {
    call(InterceptedCallTestService.class,
        new AssertionCallback<String>("Request was not intercepted", "success"))
        .interceptedGetForClientError("result");
  }
}
