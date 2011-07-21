package org.jboss.errai.ioc.rebind;

import junit.framework.Test;
import junit.runner.BaseTestRunner;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class IOCTestRunner extends Runner {

  private Class<?> toRun;

  public IOCTestRunner(Class<?> toRun) {
    this.toRun = toRun;
  }

  @Override
  public Description getDescription() {
    Description description = Description.createSuiteDescription(toRun);

    return description;
  }

  @Override
  public void run(RunNotifier notifier) {
    Object instance;

    try {
      instance = toRun.newInstance();
    }
    catch (InstantiationException e) {
      throw new RuntimeException("Could not intantiate test class: " + toRun.getName(), e);
    }
    catch (IllegalAccessException e) {
      throw new RuntimeException("Could not intantiate test class: " + toRun.getName(), e);
    }


    for (Description description : getDescription().getChildren()) {

    }
  }
}
