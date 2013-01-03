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

import com.google.gwt.http.client.RequestPermissionException;
import com.google.gwt.http.client.Response;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseException;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.enterprise.client.jaxrs.test.AbstractErraiJaxrsTest;
import org.jboss.errai.enterprise.jaxrs.client.shared.ErrorHandlingTestService;
import org.jboss.errai.enterprise.jaxrs.client.shared.PlainMethodTestService;
import org.junit.Test;

/**
 * Testing error handling features.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ErrorHandlingIntegrationTest extends AbstractErraiJaxrsTest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.enterprise.jaxrs.TestModule";
  }

  @Test
  public void testErrorHandling() {
    call(ErrorHandlingTestService.class,
        new RemoteCallback<Long>() {
          @Override
          public void callback(Long response) {
            fail("Callback should not be invoked");
          }
        },
        new ErrorCallback<Object>() {
          @Override
          public boolean error(Object message, Throwable throwable) {
            try {
              throw throwable;
            }
            catch (ResponseException e) {
              assertEquals("Wrong status code received", Response.SC_NOT_FOUND, e.getResponse().getStatusCode());
              finishTest();
            }
            catch (Throwable t) {
              fail("Expected ResponseException");
            }
            return false;
          }
        }
        ).error();

    delayTestFinish(5000);
  }
  
  @Test
  public void testErrorHandlingWithInvalidBaseUrl() {
    delayTestFinish(5000);
    
    RestClient.create(PlainMethodTestService.class, "http://somewhere.zzz/invalidpath",
        new RemoteCallback<Long>() {
          @Override
          public void callback(Long response) {
            fail("Callback should not be invoked");
          }
        },
        new ErrorCallback<Object>() {
          @Override
          public boolean error(Object message, Throwable throwable) {
            try {
              throw throwable;
            }
            catch (RequestPermissionException e) {
              assertEquals("http://somewhere.zzz/invalidpath/test/method", e.getURL());
              finishTest();
            }
            catch (Throwable t) {
              fail("Expected RequestPermissionException");
            }
            return false;
          }
        }
        ).get();
  }

  @Test
  public void testErrorHandlingUsingResponseCallback() {
    call(ErrorHandlingTestService.class,
        new ResponseCallback() {
          @Override
          public void callback(Response response) {
            fail("Callback should not be invoked");
          }
        },
        new ErrorCallback<Object>() {
          @Override
          public boolean error(Object message, Throwable throwable) {
            try {
              throw throwable;
            }
            catch (ResponseException e) {
              assertEquals("Wrong status code received", Response.SC_NOT_FOUND, e.getResponse().getStatusCode());
              finishTest();
            }
            catch (Throwable t) {
              fail("Expected ResponseException");
            }
            return false;
          }
        }
        ).error();

    delayTestFinish(5000);
  }

  @Test
  public void testErrorHandlingUsingResponseCallbackOnly() {
    RestClient.create(ErrorHandlingTestService.class,
        new ResponseCallback() {
          @Override
          public void callback(Response response) {
            assertEquals("Wrong status code received", Response.SC_NOT_FOUND, response.getStatusCode());
            finishTest();
          }
        }).error();

    delayTestFinish(5000);
  }
  
  @Test
  public void testErrorHandlingUsingSpecifiedSuccessCodes() {
    RestClient.create(PlainMethodTestService.class,
        new ResponseCallback() {
          @Override
          public void callback(Response response) {
            fail("Callback should not be invoked");
          }
        },
        new ErrorCallback<Object>() {
          @Override
          public boolean error(Object message, Throwable throwable) {
            try {
              throw throwable;
            }
            catch (ResponseException e) {
              // expected: Specified CREATED and NO_CONTENT as success codes but OK was returned
              assertEquals("Wrong status code received", Response.SC_OK, e.getResponse().getStatusCode());
              finishTest();
            }
            catch (Throwable t) {
              fail("Expected ResponseException");
            }
            return false;
          }
        },
        Response.SC_CREATED, Response.SC_NO_CONTENT).get();

    delayTestFinish(5000);
  }
}