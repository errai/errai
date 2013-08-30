package org.jboss.errai.example.client.local;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import org.jboss.errai.aerogear.api.datamanager.Store;
import org.jboss.errai.aerogear.api.pipeline.Pipe;
import org.jboss.errai.example.client.local.authentication.LoginBox;
import org.jboss.errai.example.client.local.events.ProjectRefreshEvent;
import org.jboss.errai.example.client.local.item.ProjectItem;
import org.jboss.errai.example.client.local.pipe.Projects;
import org.jboss.errai.example.shared.Project;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.List;

import static com.google.gwt.dom.client.Style.Display.NONE;
import static org.jboss.errai.example.client.local.util.Animator.show;

/**
 * @author edewit@redhat.com
 */
@Templated("App.html#project-list")
public class ProjectPanel extends Composite {
  @Inject
  private LoginBox loginBox;

  @Inject @Projects
  private Event<List<Project>> projectListEventSource;

  @Inject
  @Projects
  private Pipe<Project> pipe;

  @Inject
  private Store<Project> projectStore;

  @DataField("project-loader")
  private Element projectStatusBar = DOM.createElement("div");

  @DataField
  private Element addProject = DOM.createDiv();

  @Inject
  @DataField("project-form")
  private ProjectForm form;

  @Inject
  @DataField("project-container")
  private ListWidget<Project, ProjectItem> listWidget;

  @PostConstruct
  public void loadTasks() {
    refreshProjectList();
    projectStatusBar.getStyle().setDisplay(NONE);
  }

  private void onProjectListChanged(@Observes ProjectRefreshEvent event) {
    refreshProjectList();
  }

  private void refreshProjectList() {
    pipe.read(new AsyncCallback<List<Project>>() {
      @Override
      public void onSuccess(List<Project> result) {
        listWidget.setItems(result);
        for (Project project : result) {
          projectStore.save(project);
        }
        projectListEventSource.fire(result);
      }

      @Override
      public void onFailure(Throwable caught) {
        loginBox.show();
      }
    });
  }

  @EventHandler("addProject")
  public void onAddProjectClicked(ClickEvent event) {
    show(event.getRelativeElement());
    form.reset();
  }
}
