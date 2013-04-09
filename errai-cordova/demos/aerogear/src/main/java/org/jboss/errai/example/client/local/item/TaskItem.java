package org.jboss.errai.example.client.local.item;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.aerogear.api.pipeline.Pipe;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.example.client.local.events.TaskRefreshEvent;
import org.jboss.errai.example.client.local.events.TaskUpdateEvent;
import org.jboss.errai.example.client.local.pipe.TaskPipe;
import org.jboss.errai.example.client.local.util.DefaultCallback;
import org.jboss.errai.example.shared.Task;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.*;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import static com.google.gwt.dom.client.Style.Display.INLINE;
import static com.google.gwt.dom.client.Style.Display.NONE;

/**
 * @author edewit@redhat.com
 */
@Templated
public class TaskItem extends Composite implements HasModel<Task> {
  @Inject
  private Event<TaskUpdateEvent> taskUpdateEventSource;

  @Inject
  private Event<TaskRefreshEvent> taskRefreshEventSource;

  @Inject
  @TaskPipe
  private Pipe<Task> pipe;

  @Inject
  @AutoBound
  private DataBinder<Task> taskBinder;

  @Inject
  @Bound
  @DataField("task-title")
  private Label title;

  @Inject
  @Bound
  @DataField("task-date")
  private Label date;

  @Inject
  @Bound
  @DataField("task-desc")
  private Label description;

  @DataField
  private Element overlay = DOM.createDiv();

  @Inject
  @DataField
  private Anchor delete;

  @Inject
  @DataField
  private Anchor edit;

  @EventHandler
  public void onMouseOut(MouseOutEvent event) {
    overlay.getStyle().setDisplay(NONE);
  }

  @EventHandler
  public void onMouseOver(MouseOverEvent event) {
    overlay.getStyle().setDisplay(INLINE);
  }

  @EventHandler("delete")
  public void onDeleteClicked(ClickEvent event) {
    String id = String.valueOf(taskBinder.getModel().getId());
    pipe.remove(id, new DefaultCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        taskRefreshEventSource.fire(new TaskRefreshEvent());
      }
    });
  }

  @EventHandler("edit")
  public void onEditClicked(ClickEvent event) {
    taskUpdateEventSource.fire(new TaskUpdateEvent(taskBinder.getModel()));
  }

  @Override
  public Task getModel() {
    return taskBinder.getModel();
  }

  @Override
  public void setModel(Task model) {
    taskBinder.setModel(model, InitialState.FROM_MODEL);
  }
}
