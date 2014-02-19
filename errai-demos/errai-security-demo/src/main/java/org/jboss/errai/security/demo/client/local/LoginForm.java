package org.jboss.errai.security.demo.client.local;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.security.client.local.identity.Identity;
import org.jboss.errai.security.shared.User;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.nav.client.local.api.LoginPage;
import org.jboss.errai.ui.shared.api.annotations.*;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Page(role = LoginPage.class)
@Templated("#root")
@Dependent
public class LoginForm extends Composite {

  @Inject
  TransitionTo<WelcomePage> welcomePage;

  @Inject
  @Model
  Identity identity;

  @Inject
  @Bound
  @DataField
  private TextBox username;

  @DataField
  private Element form = DOM.createDiv();

  @Inject
  @Bound
  @DataField
  private PasswordTextBox password;

  @Inject
  @DataField
  private Anchor login;

  @Inject
  @DataField
  private Anchor logout;

  @DataField
  Element alert = DOM.createDiv();

  @EventHandler("login")
  private void loginClicked(ClickEvent event) {
    identity.login(null, new BusErrorCallback() {
      @Override
      public boolean error(Message message, Throwable throwable) {
        alert.getStyle().setDisplay(Style.Display.BLOCK);
        return false;
      }
    });
  }

  @EventHandler("logout")
  private void logoutClicked(ClickEvent event) {
    identity.logout();
    welcomePage.go();
  }

  @PageShown
  private void isLoggedIn() {
    identity.getUser(new AsyncCallback<User>() {
      @Override
      public void onSuccess(User result) {
        if (result != null) {
          form.getStyle().setDisplay(Style.Display.NONE);
          logout.getElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);
        } else {
          form.getStyle().setDisplay(Style.Display.BLOCK);
          logout.getElement().getStyle().setDisplay(Style.Display.NONE);
        }
      }

      @Override
      public void onFailure(Throwable caught) {
      }
    });
  }
}
