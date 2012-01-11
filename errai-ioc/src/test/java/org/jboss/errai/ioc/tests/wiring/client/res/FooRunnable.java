package org.jboss.errai.ioc.tests.wiring.client.res;

import org.jboss.errai.ioc.client.api.IOCBootstrapTask;
import org.jboss.errai.ioc.client.api.TaskOrder;

/**
 * @author Mike Brock
 */
@IOCBootstrapTask(TaskOrder.Before)
public class FooRunnable implements Runnable {
  @Override
  public void run() {
    System.out.println("yay!");
  }
}
