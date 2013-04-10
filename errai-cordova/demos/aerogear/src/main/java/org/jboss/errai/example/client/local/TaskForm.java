package org.jboss.errai.example.client.local;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.DateBox;
import org.jboss.errai.aerogear.api.datamanager.Store;
import org.jboss.errai.aerogear.api.pipeline.Pipe;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.example.client.local.events.ProjectRefreshEvent;
import org.jboss.errai.example.client.local.events.TagRefreshEvent;
import org.jboss.errai.example.client.local.events.TaskRefreshEvent;
import org.jboss.errai.example.client.local.events.TaskUpdateEvent;
import org.jboss.errai.example.client.local.pipe.TagStore;
import org.jboss.errai.example.client.local.pipe.TaskPipe;
import org.jboss.errai.example.client.local.util.ColorConverter;
import org.jboss.errai.example.client.local.util.DefaultCallback;
import org.jboss.errai.example.shared.Project;
import org.jboss.errai.example.shared.Tag;
import org.jboss.errai.example.shared.Task;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ui.shared.api.annotations.*;

import javax.annotation.Nullable;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.jboss.errai.example.client.local.Animator.hide;
import static org.jboss.errai.example.client.local.Animator.show;

/**
 * @author edewit@redhat.com
 */
@Dependent
@Templated("App.html#task-form")
public class TaskForm extends Composite {
  @Inject
  Event<TaskRefreshEvent> taskAddedEventSource;

  @Inject
  @AutoBound
  private DataBinder<Task> taskBinder;

  @Inject
  @TaskPipe
  private Pipe<Task> taskPipe;

  @Inject
  private Store<Project> projectStore;

  @Inject
  private TagStore tagStore;

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

  @DataField("project")
  private ValueListBox<Project> projectListBox = new ValueListBox<Project>(new Renderer<Project>() {
    @Override
    public String render(Project project) {
      if (project == null) {
        return "No Project";
      }
      return project.getTitle();
    }

    @Override
    public void render(Project project, Appendable appendable) throws IOException {
      appendable.append(render(project));
    }
  });

  @Inject
  @DataField("task-tag-column")
  private FlowPanel tags;

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
    refreshProjectList();
    refreshTagList();
  }

  private void updatedProjectList(@Observes ProjectRefreshEvent event) {
    refreshProjectList();
  }

  private void updateTagList(@Observes TagRefreshEvent event) {
    refreshTagList();
  }

  private void refreshProjectList() {
    Collection<Project> projects = projectStore.readAll();
    List<Project> result = new ArrayList<Project>(projects);
    result.add(0, null);
    projectListBox.setAcceptableValues(result);
  }

  private void refreshTagList() {
    tags.clear();
    Collection<Tag> tagCollection = tagStore.readAll();
    for (Tag tag : tagCollection) {
      CheckBox box = new CheckBox(tag.getTitle());
      box.getElement().getStyle().setBackgroundColor(new ColorConverter().toWidgetValue(tag.getStyle()));
      tags.add(box);
    }
  }

  @EventHandler("submit")
  public void onSubmit(ClickEvent event) {
    final Element div = getContainer(event);
    Task task = taskBinder.getModel();
    Project project = projectListBox.getValue();
    if (project != null) {
      task.setProject(project.getId());
    }

    task.setTags(getTagIds());
    task.setDate(date.getTextBox().getValue());
    taskPipe.save(task, new DefaultCallback<Task>() {
      @Override
      public void onSuccess(final Task newTask) {
        hide(div, new DefaultCallback<Void>() {

          @Override
          public void onSuccess(Void result) {
            taskAddedEventSource.fire(new TaskRefreshEvent());
          }
        });
      }
    });
  }

  private List<Long> getTagIds() {
    final List<String> tagNames = new ArrayList<String>();
    for (Widget tag : tags) {
      if (tag instanceof CheckBox && ((CheckBox) tag).getValue()) {
        tagNames.add(((CheckBox) tag).getText());
      }
    }

    return Lists.newArrayList(
        FluentIterable.from(tagStore.readAll()).filter(new Predicate<Tag>() {
          @Override
          public boolean apply(@Nullable Tag tag) {
            return tag != null && tagNames.contains(tag.getTitle());
          }
        }).transform(new Function<Tag, Long>() {

          @Nullable
          @Override
          public Long apply(@Nullable Tag tag) {
            return tag.getId();
          }
        }));
  }

  private void onUpdateTaks(@Observes TaskUpdateEvent event) {
    Task task = event.getTask();
    taskBinder.setModel(task, InitialState.FROM_MODEL);
    submit.setText("Update Task");
    show(asWidget().getElement().getParentElement().getPreviousSiblingElement());
  }

  @EventHandler("cancel")
  public void onCancel(ClickEvent event) {
    hide(getContainer(event));
  }

  private Element getContainer(ClickEvent event) {
    return event.getRelativeElement().getParentElement().getParentElement();
  }
}
