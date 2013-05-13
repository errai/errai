package org.jboss.errai.demo.todo.client.local;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.demo.todo.shared.RegistrationException;
import org.jboss.errai.demo.todo.shared.SignupService;
import org.jboss.errai.demo.todo.shared.User;
import org.jboss.errai.jpa.sync.client.local.ClientSyncManager;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Model;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.common.collect.ImmutableMultimap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

@Templated("#main")
@Page(path="signup")
public class SignupPage extends Composite {

  @Inject private @DataField Label overallErrorMessage;

  // TODO bean validation on user
  @Inject private @Model User user;
  @Inject private @Bound @DataField TextBox shortName;
  @Inject private @Bound @DataField TextBox fullName;
  @Inject private @Bound @DataField TextBox email;

  @Inject private @DataField PasswordTextBox password1;
  @Inject private @DataField PasswordTextBox password2;

  @Inject private @DataField Button signupButton;

  @Inject private Caller<SignupService> signupService;

  @Inject private TransitionTo<TodoListApp> todoListPageLink;

  @Inject private ClientSyncManager syncManager;

  @PostConstruct
  private void init() {
    overallErrorMessage.setVisible(false);
  }

  @EventHandler("signupButton")
  private void doSignup(ClickEvent e) {
    try {
      signupService.call(new RemoteCallback<User>() {
        @Override
        public void callback(User response) {
          Map<String, Object> params = new HashMap<String, Object>();
          params.put("userId", response.getId());
          syncManager.coldSync("userById", User.class, params);
          todoListPageLink.go(ImmutableMultimap.<String, String>of("user", response.getId().toString()));
        }
      },
      new BusErrorCallback() {
        @Override
        public boolean error(Message message, Throwable throwable) {
          overallErrorMessage.setText(throwable.getMessage());
          overallErrorMessage.setVisible(true);
          return false;
        }
      }).register(user);
    } catch (RegistrationException e1) {
      // won't happen for async remote call
    }
  }

}
