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
import org.jboss.errai.example.client.local.events.ProjectRefreshEvent;
import org.jboss.errai.example.client.local.events.ProjectUpdateEvent;
import org.jboss.errai.example.client.local.pipe.Projects;
import org.jboss.errai.example.client.local.util.ColorConverter;
import org.jboss.errai.example.client.local.util.DefaultCallback;
import org.jboss.errai.example.shared.Project;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.*;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import static com.google.gwt.dom.client.Style.Display.INLINE;
import static com.google.gwt.dom.client.Style.Display.NONE;

@Templated("#root")
public class ProjectItem extends Composite implements HasModel<Project> {
  @Inject
  private Event<ProjectUpdateEvent> projectUpdateEventSource;

  @Inject
  private Event<ProjectRefreshEvent> projectRefreshEventSource;

  @Inject
  @Projects
  Pipe<Project> projectPipe;

  @Inject @AutoBound
  private DataBinder<Project> projectDataBinder;

  @Inject
  @Bound
  @DataField("name")
  private Label title;

  @Inject
  @DataField
  private Anchor edit;

  @Inject
  @DataField
  private Anchor delete;

  @DataField
  private Element overlay = DOM.createDiv();

  @Override
  public Project getModel() {
    return projectDataBinder.getModel();
  }

  @Override
  public void setModel(Project model) {
    projectDataBinder.setModel(model, InitialState.FROM_MODEL);
    asWidget().getElement().getStyle().setBackgroundColor(getBackgroundColor(model.getStyle()));
  }

  private String getBackgroundColor(String style) {
    return new ColorConverter().toWidgetValue(style);
  }

  @EventHandler
  public void onMouseOut(MouseOutEvent event) {
    overlay.getStyle().setDisplay(NONE);
  }

  @EventHandler
  public void onMouseOver(MouseOverEvent event) {
    overlay.getStyle().setDisplay(INLINE);
  }

  @EventHandler("edit")
  public void onEditClicked(ClickEvent event) {
    projectUpdateEventSource.fire(new ProjectUpdateEvent(projectDataBinder.getModel()));
  }

  @EventHandler("delete")
  public void onDeleteClicked(ClickEvent event) {
    String id = String.valueOf(projectDataBinder.getModel().getId());
    projectPipe.remove(id, new DefaultCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        projectRefreshEventSource.fire(new ProjectRefreshEvent());
      }
    });
  }
}
