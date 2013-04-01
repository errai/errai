package org.jboss.errai.aerogear.api.pipeline.impl;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.errai.aerogear.api.pipeline.auth.Authenticator;

/**
 * @author edewit@redhat.com
 */
public class AuthenticatorAdapter implements Authenticator {

  private final JavaScriptObject auth;

  public AuthenticatorAdapter(JavaScriptObject auth) {
    this.auth = auth;
  }

  public JavaScriptObject unwrap() {
    return auth;
  }

  @Override
  public void enroll(String username, String password, AsyncCallback<String> callback) {
    enroll0(username, password, callback);
  }

  private native void enroll0(String username, String password, AsyncCallback<String> callback) /*-{
      this.@org.jboss.errai.aerogear.api.pipeline.impl.AuthenticatorAdapter::auth.enroll(
          {
              username: username,
              password: password
          },
          {
              success: function (data) {
                  callback.@com.google.gwt.user.client.rpc.AsyncCallback::onSuccess(Ljava/lang/Object;)(data.username);
              },
              error: function () {
                  callback.@com.google.gwt.user.client.rpc.AsyncCallback::onFailure(Ljava/lang/Throwable;)(null);
              }
          });

  }-*/;

  @Override
  public void login(String username, String password, AsyncCallback<String> callback) {
    login0(username, password, callback);
  }

  private native void login0(String username, String password, AsyncCallback<String> callback) /*-{
      this.@org.jboss.errai.aerogear.api.pipeline.impl.AuthenticatorAdapter::auth.login(
          {
              username: username,
              password: password
          },
          {
              success: function (data) {
                  callback.@com.google.gwt.user.client.rpc.AsyncCallback::onSuccess(Ljava/lang/Object;)(data.username);
              },
              error: function () {
                  callback.@com.google.gwt.user.client.rpc.AsyncCallback::onFailure(Ljava/lang/Throwable;)(null);
              }
          });
  }-*/;

  @Override
  public void logout(AsyncCallback<Void> callback) {
    logout0(callback);
  }

  private native void logout0(AsyncCallback<Void> callback) /*-{
      this.@org.jboss.errai.aerogear.api.pipeline.impl.AuthenticatorAdapter::auth.logout({
          success: function () {
              callback.@com.google.gwt.user.client.rpc.AsyncCallback::onSuccess(Ljava/lang/Object;)(null);
          },
          error: function () {
              callback.@com.google.gwt.user.client.rpc.AsyncCallback::onFailure(Ljava/lang/Throwable;)(null);
          }
      });
  }-*/;
}
