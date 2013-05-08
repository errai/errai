package org.jboss.errai.security.client;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.junit.Ignore;

/**
 * @author edewit@redhat.com
 */
@Ignore
public class SecurityTest extends AbstractErraiCDITest {
  @Override
  public String getModuleName() {
    return "org.jboss.errai.security.SecurityIntegrationTest";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
  }

  public void testLoginIsPreformed() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        final SecurityTestModule module = IOC.getBeanManager().lookupBean(SecurityTestModule.class).getInstance();

        module.login();
        finishTest();
      }
    });
  }
}
