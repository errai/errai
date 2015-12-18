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

package org.jboss.errai.enterprise.client.jaxrs.test;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.JaxrsModule;
import org.jboss.errai.enterprise.client.jaxrs.api.RequestCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.enterprise.client.jaxrs.api.RestErrorCallback;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.junit.client.GWTTestCase;

/**
 * Base class for Errai JAX-RS tests.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractErraiJaxrsTest extends GWTTestCase {
  protected String jaxRsApplicationRoot = "/";
  private final TestErrorCallback errorCallback = new TestErrorCallback();

  @Override
  protected void gwtSetUp() throws Exception {
    RestClient.setApplicationRoot(jaxRsApplicationRoot);
    RestClient.setJacksonMarshallingActive(false);
    new JaxrsModule().onModuleLoad();
    super.gwtSetUp();
  }

  protected <T, R> T call(Class<T> remote, RemoteCallback<R> callback, Integer... successCodes) {
    return RestClient.create(remote, callback, errorCallback, successCodes);
  }

  protected <T, R> T call(Class<T> remote, RemoteCallback<R> callback, RestErrorCallback errorCallback,
      Integer... successCodes) {
    return RestClient.create(remote, callback, errorCallback, successCodes);
  }

  protected <T, R> T call(Class<T> remote, RemoteCallback<R> callback, RestErrorCallback errorCallback,
      RequestCallback requestCallback, Integer... successCodes) {
    return RestClient.create(remote, callback, errorCallback, requestCallback, successCodes);
  }

  protected <T, R> T call(Class<T> remote, String baseUrl, RemoteCallback<R> callback, Integer... successCodes) {
    return RestClient.create(remote, baseUrl, callback, errorCallback, successCodes);
  }

  protected class AssertionCallback<T> implements RemoteCallback<T> {
    private final String msg;
    private final T expected;

    public AssertionCallback(String msg, T expected) {
      this.msg = msg;
      this.expected = expected;
      delayTestFinish(10000);
    }

    @Override
    public void callback(T response) {
      assertEquals(msg, expected, response);
      finishTest();
    }
  }

  protected class AssertionResponseCallback implements ResponseCallback {
    private final String msg;
    private final int statusCode;
    private String body;

    public AssertionResponseCallback(String msg, int statusCode) {
      this.msg = msg;
      this.statusCode = statusCode;
      delayTestFinish(5000);
    }

    public AssertionResponseCallback(String msg, int statusCode, String body) {
      this(msg, statusCode);
      this.body = body;
    }

    @Override
    public void callback(Response response) {
      assertEquals(msg, statusCode, response.getStatusCode());
      if (body != null)
        assertEquals(msg, body, response.getText());
      finishTest();
    }
  }

  private class TestErrorCallback implements RestErrorCallback {
    @Override
    public boolean error(Request request, Throwable throwable) {
      fail(throwable.toString());
      return false;
    }
  }
}
