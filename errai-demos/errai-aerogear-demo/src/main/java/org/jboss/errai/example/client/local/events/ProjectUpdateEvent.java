package org.jboss.errai.example.client.local.events;

import org.jboss.errai.example.shared.Project;

/**
 * @author edewit@redhat.com
 */
public class ProjectUpdateEvent {

  private final Project project;

  public ProjectUpdateEvent(Project project) {
    this.project = project;
  }

  public Project getProject() {
    return project;
  }
}
