package org.jboss.errai.demo.todo.client.local;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.bus.client.api.BusErrorCallback;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.demo.todo.shared.ShareService;
import org.jboss.errai.demo.todo.shared.UnknownUserException;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author edewit@redhat.com
 */
@RestrictedAccess
@Templated("#main")
@Page(path="share")
public class SharePage extends Composite {
  @Inject private @DataField Label overallErrorMessage;
  @Inject private @DataField TextBox email;
  @Inject private @DataField Button shareButton;
  @Inject private Caller<ShareService> shareService;
  @Inject private TransitionTo<TodoListPage> todoListPageLink;

  @PostConstruct
  private void init() {
    overallErrorMessage.setVisible(false);
  }

  @EventHandler("shareButton")
  private void doShare(ClickEvent e) {
    try {
      shareService.call(
              new RemoteCallback<Void>() {
                @Override
                public void callback(Void response) {
                  todoListPageLink.go();
                }
              },
              new BusErrorCallback() {
                @Override
                public boolean error(Message message, Throwable throwable) {
                  overallErrorMessage.setText(throwable.getMessage());
                  overallErrorMessage.setVisible(true);
                  return false;
                }
              }
      ).share(email.getText());
    } catch (UnknownUserException e1) {
      // won't happen for async remote call
    }
  }
}
