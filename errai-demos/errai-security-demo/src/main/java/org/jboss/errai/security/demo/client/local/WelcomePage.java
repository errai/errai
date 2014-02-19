package org.jboss.errai.security.demo.client.local;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.security.client.local.identity.Identity;
import org.jboss.errai.security.shared.LoggedInEvent;
import org.jboss.errai.security.shared.LoggedOutEvent;
import org.jboss.errai.security.shared.User;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
@Templated("#root")
@Page(startingPage = true)
public class WelcomePage extends Composite {

  static final String ANONYMOUS = "anonymous";

  @Inject
  public
  @DataField
  Button startButton;

  @Inject
  @DataField
  private Label userLabel;

  @Inject
  private Identity identity;

  @Inject
  TransitionTo<Messages> startButtonClicked;

  @EventHandler("startButton")
  public void onStartButtonPress(ClickEvent e) {
    startButtonClicked.go();
  }

  @AfterInitialization
  private void setupUserLabel() {
    identity.getUser(new AsyncCallback<User>() {
      @Override
      public void onSuccess(User user) {
        userLabel.setText(user != null ? user.getFullName() : ANONYMOUS);
      }

      @Override
      public void onFailure(Throwable caught) {
        userLabel.setText(ANONYMOUS);
      }
    });
  }

  private void onLoggedIn(@Observes LoggedInEvent loggedInEvent) {
    userLabel.setText(loggedInEvent.getUser().getFullName());
  }

  private void onLoggedOut(@Observes LoggedOutEvent loggedOutEvent) {
    userLabel.setText(ANONYMOUS);
  }
}
