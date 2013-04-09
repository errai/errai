package org.jboss.errai.example.client.local;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.TextBox;
import net.auroris.ColorPicker.client.Color;
import org.jboss.errai.aerogear.api.pipeline.Pipe;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.example.client.local.events.ProjectRefreshEvent;
import org.jboss.errai.example.client.local.events.ProjectUpdateEvent;
import org.jboss.errai.example.client.local.pipe.ProjectPipe;
import org.jboss.errai.example.client.local.util.DefaultCallback;
import org.jboss.errai.example.shared.Project;
import org.jboss.errai.ui.shared.api.annotations.*;
import org.jboss.errai.ui.shared.api.style.StyleBindingsRegistry;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import static org.jboss.errai.example.client.local.Animator.hide;
import static org.jboss.errai.example.client.local.Animator.show;

/**
 * @author edewit@redhat.com
 */
@Templated("App.html#project-form")
public class ProjectForm extends ColorPickerForm {
  @Inject
  private Event<ProjectRefreshEvent> projectRefreshEventSource;

  @Inject @AutoBound
  private DataBinder<Project> projectDataBinder;

  @Inject
  @ProjectPipe
  private Pipe<Project> projectPipe;

  @Inject
  @Bound
  @DataField("project-title")
  private TextBox title;

  @Inject
  @DataField
  private Anchor submit;

  @Inject
  @DataField
  private Anchor cancel;

  private void updateProject(@Observes ProjectUpdateEvent event) {
    Project project = event.getProject();
    projectDataBinder.setModel(project, InitialState.FROM_MODEL);
    submit.setText("Update Project");
    show(asWidget().getElement().getParentElement().getPreviousSiblingElement());
    StyleBindingsRegistry.get().updateStyles();
  }

  protected void updateModel(Color color) {
    projectDataBinder.getModel().setStyle("project-" + color.getRed() + "-" + color.getGreen() + "-" + color.getBlue());
  }

  @EventHandler("submit")
  public void onSubmitClicked(ClickEvent event) {
    final com.google.gwt.dom.client.Element div = getContainer(event);
    Project project = projectDataBinder.getModel();
    projectPipe.save(project, new DefaultCallback<Project>() {
      @Override
      public void onSuccess(final Project newProject) {
        hide(div, new DefaultCallback<Void>() {

          @Override
          public void onSuccess(Void result) {
            projectRefreshEventSource.fire(new ProjectRefreshEvent());
          }
        });
      }
    });
  }

  @EventHandler("cancel")
  public void onCancelClicked(ClickEvent event) {
    hide(getContainer(event));
  }

  private com.google.gwt.dom.client.Element getContainer(ClickEvent event) {
    return event.getRelativeElement().getParentElement().getParentElement();
  }

  public void reset() {
    projectDataBinder.setModel(new Project(), InitialState.FROM_MODEL);
    submit.setText("Add Project");
  }
}
