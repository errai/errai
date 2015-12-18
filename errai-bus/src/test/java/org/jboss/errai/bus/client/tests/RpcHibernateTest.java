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

package org.jboss.errai.bus.client.tests;

import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.tests.support.HibernateObject;
import org.jboss.errai.bus.client.tests.support.HibernateRpc;
import org.jboss.errai.bus.client.tests.support.OtherHibernateObject;
import org.jboss.errai.common.client.api.RemoteCallback;

public class RpcHibernateTest extends AbstractErraiTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.bus.ErraiBusTests";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
  }

  public void testRpcWithReturnValFromHibernate() throws Exception {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        final BusErrorCallback errorCallback = new BusErrorCallback() {
          @Override
          public boolean error(Message message, Throwable throwable) {
            // This will cause the test to timeout if the inner call has an
            // error.
            if (throwable != null)
              throwable.printStackTrace();
            fail();

            return false;
          }
        };
        MessageBuilder.createCall(new RemoteCallback<Void>() {

          @Override
          public void callback(Void response) {
            MessageBuilder.createCall(new RemoteCallback<OtherHibernateObject>() {
              @Override
              public void callback(OtherHibernateObject response) {
                finishTest();
              }
            }, errorCallback, HibernateRpc.class).getOther(1);

          }
        }, errorCallback, HibernateRpc.class).addHibernateObject(new HibernateObject(1, new OtherHibernateObject()));
      }
    });
  }

}
