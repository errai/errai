package org.jboss.errai.security.demo.client.local;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.*;
import org.jboss.errai.security.client.local.Identity;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.*;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Page(loginPage = true)
@Templated("#root")
@Dependent
public class LoginForm extends Composite {

  @Inject
  @Model
  Identity identity;

  @Inject
  @Bound
  @DataField
  private TextBox username;

  @Inject
  @Bound
  @DataField
  private PasswordTextBox password;

  @Inject
  @DataField
  private Anchor login;

  @EventHandler("login")
  private void loginClicked(ClickEvent event) {
    identity.login();
  }
}
