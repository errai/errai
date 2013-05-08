package org.jboss.errai.security.demo.client.local;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

@Templated
public class NavBar extends Composite {

  @Inject @DataField Anchor home;
  @Inject @DataField Anchor login;

  @Inject TransitionTo<WelcomePage> homeTab;
  @Inject TransitionTo<LoginForm> loginTab;

  @EventHandler("home")
  public void onHomeButtonClick(ClickEvent e) {
    homeTab.go();
  }

  @EventHandler("login")
  public void onLoginButtonClicked(ClickEvent event) {
    loginTab.go();
  }

}
