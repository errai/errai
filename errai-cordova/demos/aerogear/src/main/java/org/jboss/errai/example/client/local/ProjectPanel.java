package org.jboss.errai.example.client.local;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.aerogear.api.pipeline.Pipe;
import org.jboss.errai.example.client.local.pipe.ProjectPipe;
import org.jboss.errai.example.shared.Project;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.List;

import static com.google.gwt.dom.client.Style.Display.NONE;
import static org.jboss.errai.example.client.local.Animator.show;

/**
 * @author edewit@redhat.com
 */
@Templated("App.html#project-list")
public class ProjectPanel extends Composite {

  @Inject
  @ProjectPipe
  private Pipe<Project> pipe;

  @DataField("project-loader")
  private Element projectStatusBar = DOM.createElement("div");

  @Inject
  @DataField
  private Anchor addProject;

  @Inject
  @DataField("project-container")
  ListWidget<Project, ProjectItem> listWidget;

  @AfterInitialization
  public void loadTasks() {
    refreshProjectList();
  }

  private void refreshProjectList() {
    pipe.read(new DefaultCallback<List<Project>>() {
      @Override
      public void onSuccess(List<Project> result) {
        listWidget.setItems(result);
        projectStatusBar.getStyle().setDisplay(NONE);
      }
    });
  }

  @EventHandler("addProject")
  public void onAddProjectClicked(ClickEvent event) {
    show(event.getRelativeElement());
  }
}
