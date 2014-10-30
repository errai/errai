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

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import org.jboss.errai.enterprise.client.jaxrs.api.RequestCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.RequestCallbackImpl;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.RestErrorCallback;
import org.jboss.errai.enterprise.client.jaxrs.test.AbstractErraiJaxrsTest;
import org.jboss.errai.enterprise.jaxrs.client.shared.JaxrsResponseObjectTestService;

/**
 * Testing aborting of {@link com.google.gwt.http.client.Request}.
 */
public class AbortHttpRequestTest extends AbstractErraiJaxrsTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.enterprise.jaxrs.TestModule";
  }

  public void testRequestCallback() {
    delayTestFinish(5000);
    call(JaxrsResponseObjectTestService.class,
        new ResponseCallback() {
          @Override
          public void callback(Response response) {
            fail("Callback should not be invoked");
          }
        },
        new RestErrorCallback() {
          @Override
          public boolean error(Request message, Throwable throwable) {
            fail("ErrorCallback should not be invoked");
            return false;
          }
        }, new RequestCallback() {
          @Override
          public void callback(Request request) {
            assertNotNull(request);
            assertTrue(request.isPending());
            request.cancel();
            finishTest();
          }
        }).get();
  }

  public void testRequestCallbackImpl() {
    RequestCallbackImpl requestCallback = new RequestCallbackImpl();
    call(JaxrsResponseObjectTestService.class,
        new ResponseCallback() {
          @Override
          public void callback(Response response) {
            fail("Callback should not be invoked");
          }
        },
        new RestErrorCallback() {
          @Override
          public boolean error(Request message, Throwable throwable) {
            fail("ErrorCallback should not be invoked");
            return false;
          }
        }, requestCallback).get();
    assertTrue(requestCallback.isAlive());
    requestCallback.getRequest().cancel();
  }
}
