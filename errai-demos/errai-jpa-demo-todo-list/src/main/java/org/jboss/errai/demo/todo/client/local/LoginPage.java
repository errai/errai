package org.jboss.errai.demo.todo.client.local;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.*;
import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.security.client.local.Identity;
import org.jboss.errai.security.shared.SecurityError;
import org.jboss.errai.security.shared.User;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.*;

import javax.inject.Inject;

@Templated("#main")
@Page(path="login", role = {org.jboss.errai.security.shared.LoginPage.class, SecurityError.class})
public class LoginPage extends Composite {

  @Inject private @Model Identity identity;

  @Inject private @DataField Label loginError;

  @Inject private @Bound @DataField TextBox username;
  @Inject private @Bound @DataField PasswordTextBox password;

  @Inject private @DataField Button loginButton;
  @Inject private @DataField TransitionAnchor<SignupPage> signupLink;

  @Inject private TransitionTo<TodoListPage> toListPage;

  @PageShowing
  private void onPageShowing() {
    loginError.setVisible(false);
  }

  @EventHandler
  private void hideErrorLabel(FocusEvent e) {
    loginError.setVisible(false);
  }

  @EventHandler("loginButton")
  private void login(ClickEvent e) {
    identity.login(
            new RemoteCallback<User>() {
              @Override
              public void callback(User user) {
                toListPage.go();
              }
            }, new BusErrorCallback() {
              @Override
              public boolean error(Message message, Throwable throwable) {
                loginError.setVisible(true);
                return false;
              }
            }
    );
  }

  @EventHandler({"username", "password"})
  private void onKeyDown(KeyDownEvent event) {
    //fire change events to update the binder properties
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), password);
    DomEvent.fireNativeEvent(Document.get().createChangeEvent(), username);
    // auto-submit login form when user presses enter
    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER &&
            !username.getText().trim().equals("") &&
            !password.getText().trim().equals("")) {
      login(null);
    }
  }
}
