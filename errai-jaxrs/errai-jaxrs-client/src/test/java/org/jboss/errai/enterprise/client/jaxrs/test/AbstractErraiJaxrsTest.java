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

import java.util.function.Function;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.JaxrsModule;
import org.jboss.errai.enterprise.client.jaxrs.api.RequestCallback;
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

  protected <T, R> T call(final Class<T> remote, final RemoteCallback<R> callback, final Integer... successCodes) {
    return RestClient.create(remote, callback, errorCallback, successCodes);
  }

  protected <T, R> T call(final Class<T> remote, final RemoteCallback<R> callback, final RestErrorCallback errorCallback,
      final Integer... successCodes) {
    return RestClient.create(remote, callback, errorCallback, successCodes);
  }

  protected <T, R> T call(final Class<T> remote, final RemoteCallback<R> callback, final RestErrorCallback errorCallback,
      final RequestCallback requestCallback, final Integer... successCodes) {
    return RestClient.create(remote, callback, errorCallback, requestCallback, successCodes);
  }

  protected <T, R> T call(final Class<T> remote, final String baseUrl, final RemoteCallback<R> callback, final Integer... successCodes) {
    return RestClient.create(remote, baseUrl, callback, errorCallback, successCodes);
  }

  protected class SimpleAssertionCallback<T> implements RemoteCallback<T> {
    private final String msg;
    private final T expected;

    public SimpleAssertionCallback(final String msg, final T expected) {
      this.msg = msg;
      this.expected = expected;
      delayTestFinish(10000);
    }

    @Override
    public void callback(final T response) {
      assertEquals(msg, expected, response);
      finishTest();
    }
  }

  protected class AssertionCallback<R, T> implements RemoteCallback<R> {
    private final String msg;
    private final T expected;
    private final Function<R, T> converter;

    public AssertionCallback(final String msg, final T expected, final Function<R, T> converter) {
      this.msg = msg;
      this.expected = expected;
      this.converter = converter;
      delayTestFinish(10000);
    }

    @Override
    public void callback(final R response) {
      assertEquals(msg, expected, converter.apply(response));
      finishTest();
    }
  }

  protected class AssertionResponseCallback implements RemoteCallback<Response> {
    private final String msg;
    private final int statusCode;
    private String body;

    public AssertionResponseCallback(final String msg, final int statusCode) {
      this.msg = msg;
      this.statusCode = statusCode;
      delayTestFinish(5000);
    }

    public AssertionResponseCallback(final String msg, final int statusCode, final String body) {
      this(msg, statusCode);
      this.body = body;
    }

    @Override
    public void callback(final Response response) {
      assertEquals(msg, statusCode, response.getStatusCode());
      if (body != null)
        assertEquals(msg, body, response.getText());
      finishTest();
    }
  }

  private class TestErrorCallback implements RestErrorCallback {
    @Override
    public boolean error(final Request request, final Throwable throwable) {
      fail(throwable.toString());
      return false;
    }
  }
}
