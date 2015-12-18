package org.jboss.errai.ioc.tests.wiring.client.res;

import org.jboss.errai.ioc.client.api.IOCBootstrapTask;
import org.jboss.errai.ioc.client.api.TaskOrder;
import org.jboss.errai.ioc.client.api.TestOnly;

/**
 * @author Mike Brock
 */
@TestOnly @IOCBootstrapTask(TaskOrder.Before)
public class BeforeTask implements Runnable {
  public static boolean ran = false;

  @Override
  public void run() {
    ran = true;
    TestResultsSingleton.addItem(BeforeTask.class);
  }
}
