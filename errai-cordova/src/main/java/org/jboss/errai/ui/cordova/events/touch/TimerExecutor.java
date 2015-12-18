package org.jboss.errai.ui.cordova.events.touch;

/**
 * A simple interface to make classes testable that require timed code execution
 * 
 * @author Daniel Kurka
 * 
 */
public interface TimerExecutor {

	public void execute(CodeToRun codeToRun, int time);

	public interface CodeToRun {
		public void onExecution();
	}
}
