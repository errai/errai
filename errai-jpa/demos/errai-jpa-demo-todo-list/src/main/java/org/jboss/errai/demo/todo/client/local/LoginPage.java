package org.jboss.errai.demo.todo.client.local;

import javax.inject.Inject;

import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.demo.todo.shared.LoginService;
import org.jboss.errai.demo.todo.shared.User;
import org.jboss.errai.jpa.sync.client.local.ClientSyncManager;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.common.collect.ImmutableMultimap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

@Templated("#main")
@Page(path="login", startingPage=true)
public class LoginPage extends Composite {

  @Inject private @DataField Label loginError;

  @Inject private @DataField TextBox username;
  @Inject private @DataField PasswordTextBox password;

  @Inject private @DataField Button loginButton;
  @Inject private @DataField TransitionAnchor<SignupPage> signupLink;

  @Inject private TransitionTo<TodoListPage> toListPage;

  @Inject private Caller<LoginService> loginService;
  @Inject private ClientSyncManager syncManager;

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
    loginService.call(new RemoteCallback<User>() {
        @Override
        public void callback(final User user) {
          syncManager.getDesiredStateEm().merge(user);
          syncManager.getExpectedStateEm().merge(user);
          toListPage.go(ImmutableMultimap.of("userId", user.getId().toString()));
        }
      },
      new BusErrorCallback() {
        @Override
        public boolean error(Message message, Throwable throwable) {
          loginError.setVisible(true);
          return false;
        }
      }).logIn(username.getText(), password.getText());
  }

  @EventHandler({"username", "password"})
  private void onKeyDown(KeyDownEvent event) {
    // auto-submit login form when user presses enter
    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER &&
            !username.getText().trim().equals("") &&
            !password.getText().trim().equals("")) {
      login(null);
    }
  }
}
