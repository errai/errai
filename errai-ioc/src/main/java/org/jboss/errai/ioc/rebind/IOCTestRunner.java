package org.jboss.errai.ioc.rebind;

import com.google.gwt.junit.JUnitShell;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.jboss.errai.ioc.client.IOCClientTestCase;
import org.jboss.errai.ioc.client.InterfaceInjectionContext;
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
  public static boolean SIMULATED = Boolean.getBoolean("errai.ioc.debug.simulated_client");

  private Class<? extends TestCase> toRun;

  public IOCTestRunner(Class<? extends TestCase> toRun) {
    this.toRun = toRun;
  }

  @Override
  public Description getDescription() {
    Description description = Description.createSuiteDescription(toRun);

    for (Method method : toRun.getDeclaredMethods()) {
      if (method.getName().startsWith("test") && method.getParameterTypes().length == 0) {
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
      if (SIMULATED) {

        instance = toRun.newInstance();

        if (instance instanceof IOCClientTestCase) {
          final IOCClientTestCase iocClientTestCase = (IOCClientTestCase) instance;
          iocClientTestCase.setInitializer(new IOCClientTestCase.ContainerBootstrapper() {
            @Override
            public InterfaceInjectionContext bootstrap() {
              try {
                MockIOCGenerator mockIOCGenerator = new MockIOCGenerator();
                mockIOCGenerator.setPackageFilter(iocClientTestCase.getModulePackage());
                return mockIOCGenerator.generate().newInstance().bootstrapContainer();
              }
              catch (Exception e) {
                throw new RuntimeException("failed to run in emulated mode", e);
              }
            }
          });

          iocClientTestCase.setForcePureJava(true);
          iocClientTestCase.gwtSetUp();
        }

      }
      else {
        IOCTestSuite suite = new IOCTestSuite(toRun, notifier);
        suite.run(new TestResult());

        return;
      }


    }
    catch (InstantiationException e) {
      throw new RuntimeException("Could not intantiate test class: " + toRun.getName(), e);
    }
    catch (IllegalAccessException e) {
      throw new RuntimeException("Could not intantiate test class: " + toRun.getName(), e);
    }
    catch (Exception e) {
      throw new RuntimeException("could not bootstrap", e);
    }

    for (Description description : getDescription().getChildren()) {
      try {
        notifier.fireTestStarted(Description.createTestDescription(toRun, description.getMethodName()));
        Result result = new Result();
        toRun.getDeclaredMethod(description.getMethodName()).invoke(instance);
        notifier.fireTestRunFinished(result);

        if (!result.wasSuccessful()) {
          notifier.fireTestFailure(new Failure(description, null));
        }
        else {
          notifier.fireTestFinished(description);
        }

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
