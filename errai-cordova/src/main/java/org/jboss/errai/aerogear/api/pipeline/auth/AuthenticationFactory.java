package org.jboss.errai.aerogear.api.pipeline.auth;

import com.google.gwt.core.client.JavaScriptObject;
import org.jboss.errai.aerogear.api.pipeline.impl.AuthenticatorAdapter;

/**
 * @author edewit@redhat.com
 */
public class AuthenticationFactory {

  private native JavaScriptObject setup(String name, String baseUrl, String enroll, String login, String logout) /*-{
      return $wnd.AeroGear.Auth({
          name: name,
          settings: {
              agAuth: true,
              baseURL: baseUrl,
              endpoints: {
                  enroll: enroll,
                  login: login,
                  logout: logout
              }
          }
      }).modules[name];

  }-*/;

  public Authenticator createAuthenticator(String name) {
    return new AuthenticatorAdapter(setup(name, "/", "enroll", "login", "logout"));
  }

  public Authenticator createAuthenticator(String name, String baseUrl) {
    return new AuthenticatorAdapter(setup(name, baseUrl, "enroll", "login", "logout"));
  }

  public Authenticator createAuthenticator(String name, String baseUrl, String enroll, String login, String logout) {
    return new AuthenticatorAdapter(setup(name, baseUrl, enroll, login, logout));
  }

}
