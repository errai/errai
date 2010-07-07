package org.jboss.errai.bus.server.gae;

import org.jboss.errai.bus.client.api.AsyncTask;
import org.jboss.errai.bus.client.api.TaskManager;
import org.jboss.errai.bus.client.api.base.TimeUnit;

public class GAETaskManager implements TaskManager {
	
	
	public void execute(Runnable task) {
		task.run();			
	}

	public AsyncTask schedule(TimeUnit unit, int interval, Runnable task) {			
		throw new RuntimeException("Task scheduling not supported on GAE");
	}

	public AsyncTask scheduleRepeating(TimeUnit unit, int interval,
			Runnable task) {
		throw new RuntimeException("Task scheduling not supported on GAE");
	}
}
