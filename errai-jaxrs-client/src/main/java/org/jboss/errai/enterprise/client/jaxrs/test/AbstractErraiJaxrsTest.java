package org.jboss.errai.enterprise.client.jaxrs.test;

import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.JaxrsClientBootstrap;
import org.jboss.errai.enterprise.client.jaxrs.api.ResponseCallback;

import com.google.gwt.http.client.Response;
import com.google.gwt.junit.client.GWTTestCase;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractErraiJaxrsTest extends GWTTestCase {

  protected class AssertionCallback<T> implements RemoteCallback<T> {
    private String msg;
    private T expected;

    public AssertionCallback(String msg, T expected) {
      this.msg = msg;
      this.expected = expected;
      delayTestFinish(5000);
    }

    @Override
    public void callback(T response) {
      assertEquals(msg, expected, response);
      finishTest();
    }
  }

  protected class AssertionResponseCallback implements ResponseCallback {
    private String msg;
    private int statusCode;
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

  @Override
  public void gwtSetUp() throws Exception {
    super.gwtSetUp();

    // Unfortunately, GWTTestCase does not call our inherited module's onModuleLoad() methods
    // http://code.google.com/p/google-web-toolkit/issues/detail?id=3791
    new JaxrsClientBootstrap().onModuleLoad();
  }
}
