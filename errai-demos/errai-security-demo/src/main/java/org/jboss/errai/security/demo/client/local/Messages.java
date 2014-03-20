package org.jboss.errai.security.demo.client.local;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.RestErrorCallback;
import org.jboss.errai.security.client.local.callback.DefaultRestSecurityErrorCallback;
import org.jboss.errai.security.client.local.identity.Identity;
import org.jboss.errai.security.demo.client.shared.MessageService;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.slf4j.Logger;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

@Dependent
@Templated("#main")
@Page
public class Messages extends Composite {
  @Inject
  private Identity identity;

  @Inject
  private Caller<MessageService> messageServiceCaller;

  @Inject
  @DataField("newItemForm")
  private Label label;

  @Inject
  @DataField
  private Button hello;

  @Inject
  @DataField
  private Button ping;
  
  @Inject
  private Logger logger;

  @EventHandler("hello")
  private void onHelloClicked(ClickEvent event) {
    System.out.println("Messages.onHelloClicked");
    identity.getUser(new RemoteCallback<User>() {

      @Override
      public void callback(User response) {
        messageServiceCaller.call(
                new RemoteCallback<String>() {
                  @Override
                  public void callback(String o) {
                    label.setText(o);
                  }
                }, new DefaultRestSecurityErrorCallback()).hello();
      }
    });
  }

  @EventHandler("ping")
  private void onPingClicked(ClickEvent event) {
    messageServiceCaller.call(new RemoteCallback<String>() {
      @Override
      public void callback(String o) {
        label.setText(o);
      }
    }, new DefaultRestSecurityErrorCallback(new RestErrorCallback() {
      @Override
      public boolean error(Request message, Throwable throwable) {
        identity.getUser(new RemoteCallback<User>() {
          @Override
          public void callback(User response) {
            final String name = (response != null) ? response.getLoginName() : "Anonymous";
            logger.warn(name + " has attempted to access a protected resource!");
          }
        });
        // By returning true here, the default security redirection logic will
        // occur.
        return true;
      }
    })).ping();
  }
}
