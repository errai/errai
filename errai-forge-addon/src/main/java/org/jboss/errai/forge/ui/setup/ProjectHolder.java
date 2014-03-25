package org.jboss.errai.forge.ui.setup;

import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.ui.cdi.CommandScoped;

@CommandScoped
public class ProjectHolder {
  
  private Project project;

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

}
