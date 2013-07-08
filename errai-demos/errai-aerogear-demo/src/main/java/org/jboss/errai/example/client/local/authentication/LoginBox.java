package org.jboss.errai.example.client.local.authentication;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

@Templated("#root")
public class LoginBox extends AuthenticationDialogBox {

  @Inject
  private RegisterBox registerBox;

  @Inject
  @DataField
  private TextBox username;

  @Inject
  @DataField
  private TextBox password;

  @Inject
  @DataField
  private Anchor login;

  @Inject
  @DataField
  private Anchor cancel;

  @Inject
  @DataField
  private Anchor register;

  @Inject
  @DataField
  private Label message;

  @EventHandler("login")
  private void onLoginClicked(ClickEvent event) {
    authenticator.login(username.getText(), password.getText(), new AsyncCallback<String>() {
      @Override
      public void onSuccess(String result) {
        hide();
        refresh();
      }

      @Override
      public void onFailure(Throwable caught) {
        message.setText("Login failed, please try again");
      }
    });
  }

  @EventHandler("cancel")
  private void onCancelClicked(ClickEvent event) {
    hide();
  }

  @EventHandler("register")
  private void onRegisterClicked(ClickEvent event) {
    hide();
    registerBox.show();
  }
}
