package org.jboss.errai.example.client.local.events;

import org.jboss.errai.example.shared.Task;

/**
 * @author edewit@redhat.com
 */
public class TaskUpdateEvent {
  private final Task task;

  public TaskUpdateEvent(Task task) {
    this.task = task;
  }

  public Task getTask() {
    return task;
  }
}
