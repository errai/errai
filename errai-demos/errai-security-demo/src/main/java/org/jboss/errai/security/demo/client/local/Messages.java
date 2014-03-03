package org.jboss.errai.security.demo.client.local;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.security.client.local.identity.Identity;
import org.jboss.errai.security.demo.client.shared.MessageService;
import org.jboss.errai.security.shared.User;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
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

  @EventHandler("hello")
  private void onHelloClicked(ClickEvent event) {
    System.out.println("Messages.onHelloClicked");
    identity.getUser(new RemoteCallback<User>() {

      @Override
      public void callback(User response) {
        messageServiceCaller.call(new RemoteCallback<String>() {
                                    @Override
                                    public void callback(String o) {
                                      label.setText(o);
                                    }
                                  }
        ).hello();
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
    }).ping();
  }
}
