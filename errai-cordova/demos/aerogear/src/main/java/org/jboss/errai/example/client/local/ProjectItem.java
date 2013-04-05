package org.jboss.errai.example.client.local;

import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.example.shared.Project;
import org.jboss.errai.example.shared.Task;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import com.google.gwt.user.client.ui.Composite;

import javax.inject.Inject;

@Templated("#root")
public class ProjectItem extends Composite implements HasModel<Project> {
  @Inject @AutoBound
  private DataBinder<Project> projectDataBinder;

  @Inject
  @Bound
  @DataField("name")
  private Label title;


  @Override
  public Project getModel() {
    return projectDataBinder.getModel();
  }

  @Override
  public void setModel(Project model) {
    projectDataBinder.setModel(model);
  }
}
