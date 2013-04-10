package org.jboss.errai.example.client.local;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import org.jboss.errai.aerogear.api.pipeline.Pipe;
import org.jboss.errai.example.client.local.events.TaskRefreshEvent;
import org.jboss.errai.example.client.local.item.TaskItem;
import org.jboss.errai.example.client.local.pipe.TaskPipe;
import org.jboss.errai.example.client.local.util.DefaultCallback;
import org.jboss.errai.example.shared.Task;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.List;

import static org.jboss.errai.example.client.local.Animator.show;

/**
 * @author edewit@redhat.com
 */
@Dependent
@Templated("App.html#task-container")
public class TaskPanel extends Composite {

  @Inject
  @TaskPipe
  protected Pipe<Task> taskPipe;

  @DataField("task-loader")
  private Element taskStatusBar = DOM.createElement("div");

  @DataField
  private Element addTask = DOM.createElement("div");

  @DataField("task-list-container")
  ListWidget<Task, TaskItem> taskList = new TaskList();

  @Inject
  @DataField("task-form")
  private TaskForm taskForm;

  @AfterInitialization
  public void loadTasks() {
    refreshTaskList();
    taskStatusBar.getStyle().setDisplay(Style.Display.NONE);
  }

  private void refreshTaskList() {
    taskPipe.read(new DefaultCallback<List<Task>>() {
      @Override
      public void onSuccess(List<Task> result) {
        taskList.setItems(result);
        enableToolTips();
      }
    });
  }

  private native void enableToolTips() /*-{
      $wnd.$( "#task-list-container .swatch" ).tooltip();
  }-*/;


  public void addedTask(@Observes TaskRefreshEvent taskRefreshEvent) {
    refreshTaskList();
  }

  @EventHandler("addTask")
  public void onAddTaskClicked(ClickEvent event) {
    show(event.getRelativeElement());
  }

  /**
   * ListWidget override to be able to use {@link FlowPanel}
   */
  private class TaskList extends ListWidget<Task, TaskItem> {
    private TaskList() {
      super(new FlowPanel());
    }

    @Override
    protected Class<TaskItem> getItemWidgetType() {
      return TaskItem.class;
    }
  }
}
