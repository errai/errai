package org.jboss.errai.security.client.local;

import org.jboss.errai.common.client.framework.ProxyProvider;
import org.jboss.errai.common.client.framework.RemoteServiceProxyFactory;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.security.shared.SecurityManager;

/**
 * @author edewit@redhat.com
 */
public class SecurityTest extends AbstractErraiCDITest {

  private SpyAbstractRpcProxy spy = new SpyAbstractRpcProxy();

  @Override
  public String getModuleName() {
    return "org.jboss.errai.security.SecurityTest";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    disableBus = true;
    super.gwtSetUp();
    RemoteServiceProxyFactory.addRemoteProxy(SecurityManager.class, new ProxyProvider() {
      @Override
      public Object getProxy() {
        return spy;
      }
    });
  }

  public void testLoginIsPreformed() {
    // given
    SecurityTestModule module = IOC.getBeanManager().lookupBean(SecurityTestModule.class).getInstance();

    // when
    module.login();

    // then
    assertEquals(new Integer(1), spy.getCallCount("login"));
  }
}
