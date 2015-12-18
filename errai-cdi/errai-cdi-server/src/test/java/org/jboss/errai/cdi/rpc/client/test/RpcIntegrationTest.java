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

import org.jboss.errai.common.client.api.RemoteCallback;
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
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        RpcTestBean.getInstance().callRemoteCallerA(new RemoteCallback<String>() {
          @Override
          public void callback(String response) {
            assertEquals("fooA", response);
            finishTest();
          }
        }, "foo");

      }
    });

    delayTestFinish(60000);
  }

  public void testRpcToCDIBeanQualifiedWithB() {
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        RpcTestBean.getInstance().callRemoteCallerB(new RemoteCallback<String>() {
          @Override
          public void callback(String response) {
            assertEquals("barB", response);
            finishTest();
          }
        }, "bar");
      }
    });

    delayTestFinish(60000);
  }

  public void testRpcToUnqualifiedCDIBean() {
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        RpcTestBean.getInstance().callRemoteCaller(new RemoteCallback<String>() {
          @Override
          public void callback(String response) {
            assertEquals("bar", response);
            finishTest();
          }
        }, "bar");
      }
    });

    delayTestFinish(60000);
  }

  public void testInterceptedRpc() {
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        RpcTestBean.getInstance().callInterceptedRemoteCaller(new RemoteCallback<String>() {
          @Override
          public void callback(String response) {
            assertEquals("foo_intercepted", response);
            finishTest();
          }
        }, "foo");
      }
    });

    delayTestFinish(60000);
  }

  public void testRpcAccesssingHttpSession() {
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        RpcTestBean.getInstance().callSetSessionAttribute(new RemoteCallback<Void>() {
          @Override
          public void callback(Void response) {
            RpcTestBean.getInstance().callGetSessionAttribute(new RemoteCallback<String>() {
              @Override
              public void callback(String response) {
                assertEquals("success", response);
                finishTest();
              }
            }, "test");
          }
        }, "test", "success");
      }
    });

    delayTestFinish(60000);
  }

  /**
   * Regression test for ERRAI-282 under the CDI implementation of ErraiRPC.
   * Note that there is a similar test in ErraiBus
   * (BusCommunicationTests.testRpcToInheritedMethod), which has a strikingly
   * similar, yet independent, implementation of ErraiRPC.
   */
  public void testRpcToInheritedMethod() {
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        RpcTestBean.getInstance().callSubServiceInheritedMethod(new RemoteCallback<Integer>() {
          @Override
          public void callback(Integer response) {
            assertNotNull(response);
            assertEquals(1, (int) response);
            finishTest();
          }
        });
      }
    });

    delayTestFinish(60000);
  }

}
