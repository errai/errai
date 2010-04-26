package org.jboss.errai.bus.client.api.base;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.TaskManager;
import org.jboss.errai.bus.client.framework.TaskManagerProvider;

public class TaskManagerFactory {
    private static TaskManagerProvider provider = new TaskManagerProvider() {
        private ClientTaskManager taskManager = new ClientTaskManager();
        public TaskManager get(Message message) {
            return taskManager;
        }
    };

    public static TaskManager get(Message message) {
        return provider.get(message);
    }

    public static void setTaskManagerProvider(TaskManagerProvider p) {
        provider = p;
    }
}
