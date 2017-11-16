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

package org.jboss.errai.cdi.rpc.client.test;

import java.util.ArrayList;
import java.util.Arrays;

import org.jboss.errai.cdi.common.client.payload.GenericPayload;
import org.jboss.errai.cdi.common.client.payload.ParameterizedSubtypePayload;
import org.jboss.errai.cdi.rpc.client.RpcTestBean;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.api.CDI;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class RpcIntegrationTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.rpc.RpcTestModule";
  }

  public void testRpcToCDIBeanQualifiedWithA() {
    CDI.addPostInitTask(() -> RpcTestBean.getInstance().callRemoteCallerA(response -> {
      assertEquals("fooA", response);
      finishTest();
    }, "foo"));

    delayTestFinish(60000);
  }

  public void testRpcToCDIBeanQualifiedWithB() {
    CDI.addPostInitTask(() -> RpcTestBean.getInstance().callRemoteCallerB(response -> {
      assertEquals("barB", response);
      finishTest();
    }, "bar"));

    delayTestFinish(60000);
  }

  public void testRpcToUnqualifiedCDIBean() {
    CDI.addPostInitTask(() -> RpcTestBean.getInstance().callRemoteCaller(response -> {
      assertEquals("bar", response);
      finishTest();
    }, "bar"));

    delayTestFinish(60000);
  }

  public void testInterceptedRpc() {
    CDI.addPostInitTask(() -> RpcTestBean.getInstance()
    .callInterceptedRemoteCaller(response -> {
      assertEquals("foo_intercepted", response);
      finishTest();
    }, "foo"));

    delayTestFinish(60000);
  }

  public void testRpcAccesssingHttpSession() {
    CDI.addPostInitTask(() -> RpcTestBean.getInstance()
      .callSetSessionAttribute(response -> RpcTestBean.getInstance()
              .callGetSessionAttribute(response1 -> {
                assertEquals("success", response1);
                finishTest();
    }, "test"), "test", "success"));

    delayTestFinish(60000);
  }

  /**
   * Regression test for ERRAI-282 under the CDI implementation of ErraiRPC.
   * Note that there is a similar test in ErraiBus
   * (BusCommunicationTests.testRpcToInheritedMethod), which has a strikingly
   * similar, yet independent, implementation of ErraiRPC.
   */
  public void testRpcToInheritedMethod() {
    CDI.addPostInitTask(() -> RpcTestBean.getInstance().callSubServiceInheritedMethod(response -> {
      assertNotNull(response);
      assertEquals(1, (int) response);
      finishTest();
    }));

    delayTestFinish(60000);
  }

  public void testGenericRpcPayload() throws Exception {
    CDI.addPostInitTask(() -> {
      final GenericPayload<Double, Double> payload = new GenericPayload<>();
      payload.setA(1.0);
      payload.setB(new ArrayList<>(Arrays.asList(1.1, 2.1)));
      RpcTestBean.getInstance().callGenericRoundTrip(p -> {
        assertNotNull(p);
        assertEquals(payload, p);
        finishTest();
      }, payload);
    });

    delayTestFinish(60000);
  }

  public void testParameterizedRpcPayload() throws Exception {
    CDI.addPostInitTask(() -> {
      final GenericPayload<String, Integer> payload = new GenericPayload<>();
      payload.setA("foo");
      payload.setB(new ArrayList<>(Arrays.asList(1, 2, 3)));
      RpcTestBean.getInstance().callParameterizedRoundTrip(p -> {
        assertNotNull(p);
        assertEquals(payload, p);
        finishTest();
      }, payload);
    });

    delayTestFinish(60000);
  }

  public void testParameterizedSubtypeRpcPayload() throws Exception {
    CDI.addPostInitTask(() -> {
      final ParameterizedSubtypePayload payload = new ParameterizedSubtypePayload();
      payload.setA("foo");
      payload.setB(new ArrayList<>(Arrays.asList(1, 2, 3)));
      RpcTestBean.getInstance().callParameterizedSubtypeRoundTrip(p -> {
        assertNotNull(p);
        assertEquals(payload, p);
        finishTest();
      }, payload);
    });

    delayTestFinish(60000);
  }
  
}
