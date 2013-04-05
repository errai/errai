package org.jboss.errai.example.client.local;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.example.shared.Task;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.*;

import javax.inject.Inject;

/**
 * @author edewit@redhat.com
 */
@Templated
public class TaskItem extends Composite implements HasModel<Task> {
  @Inject @AutoBound
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

  @Override
  public Task getModel() {
    return taskBinder.getModel();
  }

  @Override
  public void setModel(Task model) {
    taskBinder.setModel(model, InitialState.FROM_MODEL);
  }
}
