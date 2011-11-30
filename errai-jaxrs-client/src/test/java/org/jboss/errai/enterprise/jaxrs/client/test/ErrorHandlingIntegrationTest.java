package org.jboss.errai.enterprise.jaxrs.client.test;

import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseException;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.enterprise.client.jaxrs.test.AbstractErraiJaxrsTest;
import org.jboss.errai.enterprise.jaxrs.client.shared.ErrorHandlingTestService;
import org.junit.Test;

import com.google.gwt.http.client.Response;

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
    RestClient.create(ErrorHandlingTestService.class,
        new RemoteCallback<Long>() {
          @Override
          public void callback(Long response) {
            fail("Callback should not be invoked");
          }
        },
        new ErrorCallback() {
          @Override
          public boolean error(Message message, Throwable throwable) {
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
  public void testErrorHandlingUsingResponseCallback() {
    RestClient.create(ErrorHandlingTestService.class,
        new ResponseCallback() {
          @Override
          public void callback(Response response) {
            fail("Callback should not be invoked");
          }
        },
        new ErrorCallback() {
          @Override
          public boolean error(Message message, Throwable throwable) {
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
}