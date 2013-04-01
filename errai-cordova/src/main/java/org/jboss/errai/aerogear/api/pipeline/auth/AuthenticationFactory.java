package org.jboss.errai.aerogear.api.pipeline.auth;

import com.google.gwt.core.client.JavaScriptObject;
import org.jboss.errai.aerogear.api.pipeline.impl.AuthenticatorAdapter;

/**
 * @author edewit@redhat.com
 */
public class AuthenticationFactory {

  private native JavaScriptObject setup(String name) /*-{
      return $wnd.AeroGear.Auth([{
          name: name,
          settings: {
              agAuth: true
          }
      }]).modules[name];
  }-*/;

  public Authenticator createAuthenticator(String name) {
    return new AuthenticatorAdapter(setup(name));
  }

}
