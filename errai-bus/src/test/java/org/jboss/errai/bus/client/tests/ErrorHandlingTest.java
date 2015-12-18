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

package org.jboss.errai.bus.client.tests;

import org.jboss.errai.bus.client.framework.Wormhole;

/**
 * Error handling tests
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ErrorHandlingTest extends AbstractErraiTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.bus.ErraiBusTests";
  }

  private Throwable caught;
  private String originalServiceEntryPoint;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    super.gwtTearDown();
    if (originalServiceEntryPoint != null) {
      Wormhole.changeBusEndpointUrl(bus, originalServiceEntryPoint);
    }
  }


  public void testNoop() {
  }

//  public void testBasicErrorHandling() {
//    caught = null;
//
//    runAfterInit(new Runnable() {
//      @Override
//      public void run() {
//
//        // this is just to get a status code other than 200 ->
//        // TransportIOException
//        originalServiceEntryPoint = Wormhole.changeBusEndpointUrl(bus, "invalid.url");
//
//        bus.subscribe(DefaultErrorCallback.CLIENT_ERROR_SUBJECT, new MessageCallback() {
//          @Override
//          public void callback(Message message) {
//            caught = message.get(Throwable.class, MessageParts.Throwable);
//            assertNotNull("Throwable is null.", caught);
//            try {
//              throw caught;
//            } catch (TransportIOException e) {
//              finishTest();
//            } catch (Throwable throwable) {
//              fail("Received wrong Throwable.");
//            }
//          }
//        });
//      }
//    });
//  }
}
