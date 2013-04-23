package org.jboss.errai.aerogear.api.pipeline.auth;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author edewit@redhat.com
 */
public interface Authenticator {

  void enroll(User user, AsyncCallback<String> callback);
  void login(String username, String password, AsyncCallback<String> callback);
  void logout(AsyncCallback<Void> callback);
}
