package org.jboss.errai.security.demo.client.local;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.security.client.local.identity.Identity;
import org.jboss.errai.security.client.local.nav.PageReturn;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.nav.client.local.api.LoginPage;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Model;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

@Page(role = LoginPage.class)
@Templated("#root")
@Dependent
public class LoginForm extends Composite {

  @Inject
  TransitionTo<WelcomePage> welcomePage;
  
  @Inject
  PageReturn pageReturn;

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
    identity.login(new RemoteCallback<User>() {
      
      @Override
      public void callback(final User response) {
        if (response != null) {
          pageReturn.goBackOrHome();
        }
      }
    }, new BusErrorCallback() {
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

  @PageShowing
  private void isLoggedIn() {
    identity.getUser(new RemoteCallback<User>() {

      @Override
      public void callback(final User response) {
        if (response != null) {
          form.getStyle().setDisplay(Style.Display.NONE);
          logout.getElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);
        } else {
          form.getStyle().setDisplay(Style.Display.BLOCK);
          logout.getElement().getStyle().setDisplay(Style.Display.NONE);
        }
      }
    });
  }
}
