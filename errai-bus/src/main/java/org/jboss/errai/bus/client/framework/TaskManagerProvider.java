package org.jboss.errai.bus.client.framework;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.TaskManager;


public interface TaskManagerProvider {
    public TaskManager get(Message message);
}
