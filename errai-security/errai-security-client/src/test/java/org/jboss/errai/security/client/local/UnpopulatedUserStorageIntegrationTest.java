package org.jboss.errai.security.client.local;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.marshalling.client.Marshalling;
import org.jboss.errai.security.client.local.api.SecurityContext;
import org.jboss.errai.security.shared.api.UserCookieEncoder;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;

import com.google.gwt.user.client.Cookies;

/**
 * Tests for proper behaviour when the app loads up and the remembered user
 * cookie is not present. See also
 * {@link PrePopulatedUserStorageIntegrationTest} for tests where the cookie is
 * set when the page initially loads.
 */
public class UnpopulatedUserStorageIntegrationTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.security.SecurityTest";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    assertTrue(Cookies.isCookieEnabled());
    Cookies.removeCookie(UserCookieEncoder.USER_COOKIE_NAME);
    super.gwtSetUp();
  }

  public void testRememberUserAfterSuccessfulLogin() throws Exception {
    asyncTest();

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        final SecurityContext securityContext = IOC.getBeanManager().lookupBean(SecurityContext.class).getInstance();

        // ensure we're starting with a clean slate
        assertEquals(User.ANONYMOUS, securityContext.getCachedUser());

        MessageBuilder.createCall(new RemoteCallback<User>() {
          @Override
          public void callback(User response) {
            assertEquals(response, securityContext.getCachedUser());
            String expectedCookieValue = Marshalling.toJSON(response);
            assertEquals(expectedCookieValue, Cookies.getCookie(UserCookieEncoder.USER_COOKIE_NAME));
            finishTest();
          }
        }, AuthenticationService.class).login("user", "password");
      }
    });
  }
}
