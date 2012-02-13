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

package org.jboss.errai.cdi.integration.client.test;

import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.cdi.integration.client.shared.RpcTestBean;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.api.CDI;

/**
 * @author Mike Brock
 */
public class RpcIntegrationTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.integration.RpcTestModule";
  }

  public void testRPCCallToCDIBeanQualifiedWithA() {
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

  public void testRPCCallToCDIBeanQualifiedWithB() {
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

  public void testRPCCallToUnqualifiedCDIBean() {
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
}