package org.jboss.errai.security.demo.client.local;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.security.shared.RequireRoles;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

@Templated
public class NavBar extends Composite {

  @Inject @DataField Anchor messages;
  @Inject @DataField Anchor login;
  @Inject @DataField @RequireRoles("admin") Anchor admin;

  @Inject TransitionTo<Messages> messagesTab;
  @Inject TransitionTo<LoginForm> loginTab;
  @Inject TransitionTo<AdminPage> adminTab;

  @EventHandler("messages")
  public void onHomeButtonClick(ClickEvent e) {
    messagesTab.go();
  }

  @EventHandler("login")
  public void onLoginButtonClicked(ClickEvent event) {
    loginTab.go();
  }

  @EventHandler("admin")
  public void onAdminTabClicked(ClickEvent event) {
    adminTab.go();
  }
}
