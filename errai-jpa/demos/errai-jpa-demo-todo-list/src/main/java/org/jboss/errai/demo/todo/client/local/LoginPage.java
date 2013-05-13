package org.jboss.errai.demo.todo.client.local;

import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

@Templated("#main")
@Page(path="login", startingPage=true)
public class LoginPage extends Composite {

  @Inject private @DataField TextBox username;
  @Inject private @DataField PasswordTextBox password;

  @Inject private @DataField Button loginButton;
  @Inject private @DataField TransitionAnchor<SignupPage> signupLink;

  @EventHandler
  private void login(ClickEvent e) {
    Window.alert("Logging you in");
  }
}
