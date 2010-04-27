package org.jboss.errai.bus.client.api.base;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.TaskManager;
import org.jboss.errai.bus.client.framework.TaskManagerProvider;

public class TaskManagerFactory {
    private static TaskManagerProvider provider = new TaskManagerProvider() {
        private ClientTaskManager taskManager = new ClientTaskManager();
        public TaskManager get() {
            return taskManager;
        }
    };

    public static TaskManager get() {
        return provider.get();
    }

    public static void setTaskManagerProvider(TaskManagerProvider p) {
        provider = p;
    }
}
