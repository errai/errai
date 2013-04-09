package org.jboss.errai.example.client.local;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.DateBox;
import org.jboss.errai.aerogear.api.pipeline.Pipe;
import org.jboss.errai.example.client.local.events.TaskAddedEvent;
import org.jboss.errai.example.client.local.pipe.TaskPipe;
import org.jboss.errai.example.client.local.util.DefaultCallback;
import org.jboss.errai.example.shared.Task;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ui.shared.api.annotations.*;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import static org.jboss.errai.example.client.local.Animator.hide;

/**
 * @author edewit@redhat.com
 */
@Dependent
@Templated("App.html#task-form")
public class TaskForm extends Composite {
  @Inject
  Event<TaskAddedEvent> taskAddedEventSource;

  @Inject
  @Model
  private Task task;

  @Inject
  @TaskPipe
  private Pipe<Task> taskPipe;

  @Inject
  @Bound
  @DataField("task-title")
  private TextBox title;

  @Inject
  @DataField
  private DateBox date;

  @Inject
  @Bound
  @DataField
  private TextArea description;

  @Inject
  @DataField
  private ListBox project;

  @Inject
  @DataField
  private Anchor submit;

  @Inject
  @DataField
  private Anchor cancel;

  @AfterInitialization
  private void settings() {
    DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyy-MM-dd");
    date.setFormat(new DateBox.DefaultFormat(dateFormat));
  }

  @EventHandler("submit")
  public void onSubmit(ClickEvent event) {
    final Element div = getContainer(event);
    task.setDate(date.getTextBox().getValue());
    taskPipe.save(task, new DefaultCallback<Task>() {
      @Override
      public void onSuccess(final Task newTask) {
        hide(div, new DefaultCallback<Void>() {

          @Override
          public void onSuccess(Void result) {
            taskAddedEventSource.fire(new TaskAddedEvent(newTask));
          }
        });
      }
    });
  }

  @EventHandler("cancel")
  public void onCancel(ClickEvent event) {
    hide(getContainer(event));
  }

  private Element getContainer(ClickEvent event) {
    return event.getRelativeElement().getParentElement().getParentElement();
  }
}
