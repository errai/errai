package org.jboss.errai.example.client.local.item;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.aerogear.api.pipeline.Pipe;
import org.jboss.errai.example.client.local.events.ProjectRefreshEvent;
import org.jboss.errai.example.client.local.events.ProjectUpdateEvent;
import org.jboss.errai.example.client.local.pipe.Projects;
import org.jboss.errai.example.client.local.util.ColorConverter;
import org.jboss.errai.example.client.local.util.DefaultCallback;
import org.jboss.errai.example.shared.Project;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

@Templated("#root")
public class ProjectItem extends AbstractItem<Project, ProjectRefreshEvent, ProjectUpdateEvent> {
  @Inject
  @Projects
  Pipe<Project> projectPipe;

  @Override
  protected void afterModelSet(Project model) {
    new ColorConverter().applyStyles(asWidget().getElement().getStyle(), model.getStyle());
  }

  @EventHandler("edit")
  public void onEditClicked(ClickEvent event) {
    updateEventSource.fire(new ProjectUpdateEvent(dataBinder.getModel()));
  }

  @EventHandler("delete")
  public void onDeleteClicked(ClickEvent event) {
    String id = String.valueOf(dataBinder.getModel().getId());
    projectPipe.remove(id, new DefaultCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        refreshEventSource.fire(new ProjectRefreshEvent());
      }
    });
  }
}
