package org.jboss.errai.security.test.page.client;

import org.jboss.errai.common.client.framework.ProxyProvider;
import org.jboss.errai.common.client.framework.RemoteServiceProxyFactory;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.security.client.local.SpyAbstractRpcProxy;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.test.page.client.res.RequireAuthenticationPage;
import org.jboss.errai.security.test.page.client.res.RequiresRoleBasedPage;

/**
 * @author edewit@redhat.com
 */
public class AuthenticationTest extends AbstractErraiCDITest {
  private SpyAbstractRpcProxy spy = new SpyAbstractRpcProxy();

  @Override
  public String getModuleName() {
    return "org.jboss.errai.security.test.page.Test";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    disableBus = true;
    super.gwtSetUp();
    RemoteServiceProxyFactory.addRemoteProxy(AuthenticationService.class, new ProxyProvider() {
      @Override
      public Object getProxy() {
        return spy;
      }
    });
  }

  public void testShouldCallSecurityInterceptorOnRequiredRoleAnnotatedType() {
    // when
    IOC.getBeanManager().lookupBean(RequiresRoleBasedPage.class).getInstance();

    // then
    assertEquals(new Integer(1), spy.getCallCount("isLoggedIn"));
  }

  public void testShouldCallSecurityInterceptorOnRequiredRoleAnnotatedTypeWhenLoggedIn() {
    // given
    final SpyAbstractRpcProxy rpcProxy = new SpyAbstractRpcProxy() {
      @Override
      @SuppressWarnings("unchecked")
      public boolean isLoggedIn() {
        remoteCallback.callback(true);
        return true;
      }
    };
    RemoteServiceProxyFactory.addRemoteProxy(AuthenticationService.class, new ProxyProvider() {
      @Override
      public Object getProxy() {
        return rpcProxy;
      }
    });

    // when
    IOC.getBeanManager().lookupBean(RequiresRoleBasedPage.class).getInstance();

    // then
    assertEquals(new Integer(1), rpcProxy.getCallCount("getRoles"));
  }


  public void testShouldCallSecurityInterceptorOnRequireAuthentication() {
    // when
    IOC.getBeanManager().lookupBean(RequireAuthenticationPage.class).getInstance();

    // then
    assertEquals(new Integer(1), spy.getCallCount("isLoggedIn"));
  }
}
