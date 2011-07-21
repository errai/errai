package org.jboss.errai.ioc.rebind;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

    for (Method method : toRun.getDeclaredMethods()) {
      if (method.isAnnotationPresent(Test.class)) {
        Description testMethod = Description.createTestDescription(toRun, method.getName());

        description.addChild(testMethod);
      }
    }

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
      try {
        toRun.getDeclaredMethod(description.getMethodName()).invoke(instance);
        notifier.fireTestRunFinished(new Result());
      }
      catch (InvocationTargetException e) {
        notifier.fireTestFailure(new Failure(description, e.getTargetException()));
      }
      catch (Exception e) {
        notifier.fireTestFailure(new Failure(description, e));
      }
    }
  }
}
