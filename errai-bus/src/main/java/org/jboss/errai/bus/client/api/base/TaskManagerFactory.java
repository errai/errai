package org.jboss.errai.bus.client.api.base;

import org.jboss.errai.bus.client.api.TaskManager;
import org.jboss.errai.bus.client.framework.TaskManagerProvider;

public class TaskManagerFactory {

    private static final Object lock = new Object();
    private static TaskManagerProvider provider;

    public static TaskManager get() {
        synchronized (lock) {
            if (provider == null) {
                _initForClient();
            }
            return provider.get();
        }

    }

    private static void _initForClient() {
        provider = new TaskManagerProvider() {
            private ClientTaskManager taskManager = new ClientTaskManager();

            public TaskManager get() {
                return taskManager;
            }
        };
    }

    public static void setTaskManagerProvider(TaskManagerProvider p) {
        provider = p;
    }
}
