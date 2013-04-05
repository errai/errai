package org.jboss.errai.example.client.local.events;

import org.jboss.errai.example.shared.Task;

/**
* @author edewit@redhat.com
*/
public class TaskAddedEvent {
  private final Task task;

  public Task getTask() {
    return task;
  }

  public TaskAddedEvent(Task result) {
    this.task = result;
  }
}
