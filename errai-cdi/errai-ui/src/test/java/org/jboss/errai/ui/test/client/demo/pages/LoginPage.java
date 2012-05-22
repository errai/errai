package org.jboss.errai.ui.test.client.demo.pages;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.Insert;
import org.jboss.errai.ui.shared.api.annotations.Page;
import org.jboss.errai.ui.test.client.demo.MyPageTemplate;
import org.jboss.errai.ui.test.client.demo.component.ContentFragment;
import org.jboss.errai.ui.test.client.demo.qualifiers.Login;
import org.jboss.errai.ui.test.client.demo.service.AuthenticationService;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

@Page("/login")
public class LoginPage extends MyPageTemplate {

  @Insert @Login
  ContentFragment content;

  @Inject
  private AuthenticationService authService;

  @PostConstruct
  public void init() {
    content.getBigButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        authService.login();
      }
    });
  }
}
