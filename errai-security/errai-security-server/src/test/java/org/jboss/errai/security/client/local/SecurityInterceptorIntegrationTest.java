package org.jboss.errai.security.client.local;

import static org.junit.Assert.*;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.security.client.shared.ServiceInterface;
import org.junit.Test;

@SuppressWarnings("unused")
public class SecurityInterceptorIntegrationTest extends AbstractErraiCDITest {

  public static final long TIMEOUT = 20000;

  @Override
  public String getModuleName() {
    return "org.jboss.errai.security.SecurityIntegrationTest";
  }

  @Test
  public void testAuthenticationInterceptor() throws Exception {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        MessageBuilder.createCall(new RemoteCallback<Void>() {
          @Override
          public void callback(Void response) {
            fail();
          }
        }, ServiceInterface.class).annotatedServiceMethod();
      }
    });
  }

}
