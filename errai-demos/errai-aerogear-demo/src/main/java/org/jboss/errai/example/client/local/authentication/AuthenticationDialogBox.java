package org.jboss.errai.example.client.local.authentication;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.errai.aerogear.api.pipeline.auth.Authenticator;
import org.jboss.errai.example.client.local.events.ProjectRefreshEvent;
import org.jboss.errai.example.client.local.events.TagRefreshEvent;
import org.jboss.errai.example.client.local.events.TaskRefreshEvent;
import org.jboss.errai.ioc.client.api.AfterInitialization;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * @author edewit@redhat.com
 */
public class AuthenticationDialogBox extends Composite {
  @Inject
  Authenticator authenticator;

  @Inject
  private Event<ProjectRefreshEvent> projectRefreshEventSource;

  @Inject
  private Event<TagRefreshEvent> tagRefreshEventSource;

  @Inject
  private Event<TaskRefreshEvent> taskRefreshEventSource;

  DialogBox dialogBox = new DialogBox();

  @PostConstruct
  public void setupDialog() {
    dialogBox.setModal(true);
    dialogBox.setWidget(this);
    dialogBox.setPopupPosition(Window.getClientWidth() / 2, Window.getClientHeight() / 2);
    dialogBox.setStyleName("gwtBox");
    RootPanel.get().add(dialogBox);
    show();hide();
  }

  public void refresh() {
    //there must be a nice way to do this with qualifiers.
    projectRefreshEventSource.fire(new ProjectRefreshEvent());
    tagRefreshEventSource.fire(new TagRefreshEvent());
    taskRefreshEventSource.fire(new TaskRefreshEvent());
  }

  public void show() {
    dialogBox.show();
  }

  public void hide() {
    dialogBox.hide();
  }
}
