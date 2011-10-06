package org.jboss.errai.enterprise.client.jaxrs.test;

import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.JaxrsClientBootstrap;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractErraiJaxrsTest extends GWTTestCase {
  
  public class AssertionCallback<T> implements RemoteCallback<T> {
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
  
  @Override
  public void gwtSetUp() throws Exception {
    super.gwtSetUp();

    // Unfortunately, GWTTestCase does not call our inherited module's onModuleLoad() methods
    // http://code.google.com/p/google-web-toolkit/issues/detail?id=3791
    new JaxrsClientBootstrap().onModuleLoad();
  }
}
